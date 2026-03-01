package com.dailytable.dailytable.domain.ingredient;

import com.dailytable.dailytable.domain.gacha.GachaDto;
import com.dailytable.dailytable.global.ai.GeminiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    // Regex: only allow Korean/English letters, numbers, spaces, hyphens
    private static final Pattern VALID_INGREDIENT_PATTERN =
            Pattern.compile("^[가-힣a-zA-Z0-9\\s\\-()]+$");

    // Known non-food keywords to reject
    private static final Set<String> BLACKLIST = Set.of(
            "사랑", "행복", "엄마", "아빠", "추억", "마음", "정성", "감동",
            "우정", "눈물", "기쁨", "슬픔", "분노", "희망", "꿈", "영혼",
            "love", "happiness", "soul", "memory", "dream", "hope",
            "돈", "money", "시간", "time", "공기", "air", "바람", "wind"
    );

    // Unit words to strip during normalization (stage 1)
    private static final Set<String> UNIT_WORDS = Set.of(
            "개", "쪽", "큰술", "작은술", "티스푼", "스푼", "컵", "줌", "꼬집",
            "g", "ml", "kg", "l", "oz", "lb", "cup", "cups", "tbsp", "tsp",
            "cloves", "clove", "pieces", "piece", "slices", "slice"
    );

    // Adjectives/states to strip
    private static final Set<String> ADJECTIVES = Set.of(
            "큰", "작은", "다진", "썬", "잘게", "굵게", "얇게", "두껍게",
            "신선한", "냉동", "해동", "삶은", "구운", "볶은", "데친",
            "large", "small", "big", "fresh", "organic", "frozen", "chopped",
            "minced", "sliced", "diced", "grilled", "boiled", "fried"
    );

    public IngredientService(IngredientRepository ingredientRepository,
                             GeminiClient geminiClient,
                             ObjectMapper objectMapper) {
        this.ingredientRepository = ingredientRepository;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Validate a list of ingredient inputs. Returns only valid ones.
     */
    public List<GachaDto.ValidationResult> validateIngredients(List<GachaDto.IngredientInput> inputs) {
        List<GachaDto.ValidationResult> results = new ArrayList<>();
        if (inputs == null) return results;

        for (GachaDto.IngredientInput input : inputs) {
            String name = input.getName() != null ? input.getName().trim() : "";
            if (name.isEmpty()) {
                continue;
            }

            // Check regex pattern
            if (!VALID_INGREDIENT_PATTERN.matcher(name).matches()) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("허용되지 않는 문자가 포함되어 있습니다").build());
                continue;
            }

            // Check blacklist
            String lowerName = name.toLowerCase();
            boolean blacklisted = BLACKLIST.stream()
                    .anyMatch(word -> lowerName.contains(word));
            if (blacklisted) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("재료가 아닌 항목입니다").build());
                continue;
            }

            // Check minimum length
            if (name.length() < 1 || name.length() > 50) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("재료명은 1~50자여야 합니다").build());
                continue;
            }

            results.add(GachaDto.ValidationResult.builder()
                    .valid(true).name(name).reason(null).build());
        }

        return results;
    }

    /**
     * 3-stage normalization:
     * Stage 1: Code-based preprocessing (remove numbers, units, adjectives)
     * Stage 2: Alias DB lookup
     * Stage 3: AI normalization (minimized usage)
     */
    public String normalize(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return rawName;
        }

        // Stage 1: Code preprocessing
        String cleaned = preprocessName(rawName.trim());

        // Stage 2: Alias lookup
        String aliasResult = ingredientRepository.findNormalizedByAlias(cleaned);
        if (aliasResult != null && !aliasResult.isEmpty()) {
            return aliasResult;
        }

        // Also try the raw name for alias
        if (!cleaned.equals(rawName.trim())) {
            aliasResult = ingredientRepository.findNormalizedByAlias(rawName.trim());
            if (aliasResult != null && !aliasResult.isEmpty()) {
                return aliasResult;
            }
        }

        // Stage 3: AI normalization (only if alias not found)
        try {
            String aiResult = geminiClient.normalizeIngredient(rawName);
            JsonNode node = objectMapper.readTree(aiResult);
            String normalized = node.path("normalized").asText(cleaned);
            if (!normalized.isEmpty()) {
                // Save to alias table for future lookups
                IngredientEntity.Alias alias = IngredientEntity.Alias.builder()
                        .aliasName(rawName.trim())
                        .normalizedName(normalized)
                        .build();
                try {
                    ingredientRepository.insertAlias(alias);
                    ingredientRepository.insertIngredient(normalized);
                } catch (Exception e) {
                    log.warn("Failed to save alias: {}", e.getMessage());
                }
                return normalized;
            }
        } catch (Exception e) {
            log.warn("AI normalization failed for '{}': {}", rawName, e.getMessage());
        }

        return cleaned;
    }

    /**
     * Stage 1: Code-based preprocessing
     */
    private String preprocessName(String name) {
        String result = name;

        // Remove numbers (including decimals)
        result = result.replaceAll("[0-9]+\\.?[0-9]*", "").trim();

        // Remove unit words
        for (String unit : UNIT_WORDS) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(unit) + "\\b", "").trim();
            // Also handle Korean unit words without word boundaries
            if (unit.matches("[가-힣]+")) {
                result = result.replace(unit, "").trim();
            }
        }

        // Remove adjectives
        for (String adj : ADJECTIVES) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(adj) + "\\b", "").trim();
            if (adj.matches("[가-힣]+")) {
                result = result.replace(adj, "").trim();
            }
        }

        // Clean up extra whitespace
        result = result.replaceAll("\\s+", " ").trim();

        return result.isEmpty() ? name : result;
    }
}
