package com.dailytable.dailytable.global.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class ImageGenerationClient {

    @Value("${gemini.api.image.model}")
    private String IMAGEN_MODEL;

    @Value("${google.cloud.project.id}")
    private String projectId;

    private static final String IMAGEN_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/imagen-4.0-generate-001:predict";

    // Static images directory (relative to project root - works with bootRun)
    private static final String STATIC_IMAGE_DIR = "src/main/resources/static/images";
    private static final String IMAGE_URL_PREFIX = "/images/";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ImageGenerationClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a food image using Imagen model.
     * Falls back to Pollinations.ai if Imagen fails.
     */
    public String generateImageUrl(String title, String cuisineStyle) throws Exception {
        try {
            return generateImagenImageUrl(title, cuisineStyle);
        } catch (Exception e) {
            log.warn("Imagen image generation failed, falling back to Pollinations: {}", e.getMessage());
            return generatePollinationsUrl(title, cuisineStyle);
        }
    }


    private String generateImagenImageUrl(String title, String cuisineStyle) throws Exception {
        log.info("Attempting Imagen image generation for title: {}, cuisine: {}", title, cuisineStyle);
        String prompt = "Professional food photography of " + title
                + " 100mm macro lens, white ceramic plate,"
                + " restaurant quality, soft studio lighting, appetizing, no text";

        // Build request body for Imagen
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode instances = requestBody.putArray("instances");
        ObjectNode instance = instances.addObject();
        instance.put("prompt", prompt);
        ObjectNode parameters = requestBody.putObject("parameters");
        parameters.put("sampleCount", 1);
        parameters.put("aspectRatio", "1:1");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey); // Use header for API key
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        String apiUrl = String.format(IMAGEN_API_URL, projectId, IMAGEN_MODEL);
        log.info("Imagen API URL: {}", apiUrl);
        log.info("Request body: {}", requestBody.toString());
        try {
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            log.info("Imagen response: {}", response);

            JsonNode responseNode = objectMapper.readTree(response);
            JsonNode predictions = responseNode.path("predictions");
            if (predictions.isArray() && predictions.size() > 0) {
                String base64Data = predictions.get(0).path("bytesBase64Encoded").asText();
                if (base64Data != null && !base64Data.isEmpty()) {
                    // Save image to static directory
                    String filename = UUID.randomUUID() + ".png";
                    Path imagePath = Paths.get(STATIC_IMAGE_DIR, filename);
                    Files.createDirectories(imagePath.getParent());
                    Files.write(imagePath, Base64.getDecoder().decode(base64Data));

                    log.info("Imagen image generated and saved: {}", filename);
                    return IMAGE_URL_PREFIX + filename;
                }
            }
            throw new RuntimeException("No image data in Imagen response");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Imagen API error: Status {}, Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    /**
     * Fallback: Pollinations.ai URL-based generation (free, no API key needed)
     */
    private String generatePollinationsUrl(String title, String cuisineStyle) {

        String shortTitle = title != null && title.length() > 20
                ? title.substring(0, 20)
                : title;

        String prompt = String.join(", ",
                shortTitle,
                cuisineStyle + " cuisine",
                "restaurant plating",
                "professional food photography",
                "ultra realistic texture",
                "natural soft daylight",
                "50mm lens",
                "shallow depth of field",
                "close-up shot",
                "high detail",
                "steam visible",
                "rich color contrast",
                "no text",
                "no watermark",
                "no logo",
                "no people",
                "no hands",
                "clean background"
        );

        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);

        String url = "https://image.pollinations.ai/prompt/" + encodedPrompt
                + "?width=768"
                + "&height=768"
                + "&nologo=true"
                + "&model=flux"
                + "&seed=42"; // 고정 seed로 재현성 확보

        return url.length() > 500 ? url.substring(0, 500) : url;
    }
}
