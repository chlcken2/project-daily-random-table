package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.ingredient.IngredientService;
import com.dailytable.dailytable.domain.recipe.RecipeEntity;
import com.dailytable.dailytable.domain.recipe.RecipeService;
import com.dailytable.dailytable.global.ai.GeminiClient;
import com.dailytable.dailytable.global.ai.ImageGenerationClient;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GachaService {

    private static final int MAX_DAILY = 3;
    private static final int MAX_RETRY = 2;

    private static final Map<String, Integer> PURPOSE_MAP = Map.of(
            "속세의맛", 1, "다이어트", 2, "건강식", 3, "술안주", 4
    );
    private static final Map<String, Integer> CUISINE_MAP = Map.of(
            "상관없음", 1, "한식", 2, "일식", 3, "중식", 4, "양식", 5, "동남아", 6
    );
    private static final Map<String, Integer> DIFFICULTY_MAP = Map.of(
            "상관없음", 0, "하", 1, "중", 2, "상", 3
    );
    private static final Map<String, String> DIFFICULTY_AI_MAP = Map.of(
            "하", "LOW", "중", "MEDIUM", "상", "HIGH", "상관없음", "ANY"
    );
    private static final Map<String, Integer> DIFFICULTY_RESULT_MAP = Map.of(
            "LOW", 1, "MEDIUM", 2, "HIGH", 3
    );
    private static final Map<String, String> DIFFICULTY_LABEL_MAP = Map.of(
            "LOW", "하", "MEDIUM", "중", "HIGH", "상"
    );
    private static final Map<Integer, String> CUISINE_STYLE_MAP = Map.of(
            1, "Any", 2, "Korean", 3, "Japanese", 4, "Chinese", 5, "Western", 6, "Southeast Asian"
    );

    private final GachaRepository gachaRepository;
    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final GeminiClient geminiClient;
    private final ImageGenerationClient imageGenerationClient;
    private final ObjectMapper objectMapper;

    public GachaService(GachaRepository gachaRepository,
                        RecipeService recipeService,
                        IngredientService ingredientService,
                        GeminiClient geminiClient,
                        ImageGenerationClient imageGenerationClient,
                        ObjectMapper objectMapper) {
        this.gachaRepository = gachaRepository;
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.geminiClient = geminiClient;
        this.imageGenerationClient = imageGenerationClient;
        this.objectMapper = objectMapper;
    }

    public GachaDto.DailyCountResponse getDailyCount(Long userId) {
        int count = gachaRepository.countTodayGenerations(userId);
        return GachaDto.DailyCountResponse.builder()
                .count(count)
                .max(MAX_DAILY)
                .canGenerate(count < MAX_DAILY)
                .build();
    }

    public GachaDto.GenerateResponse generate(GachaDto.GenerateRequest request, Long userId) {
        // Check daily limit
        int todayCount = gachaRepository.countTodayGenerations(userId);
        if (todayCount >= MAX_DAILY) {
            throw new GachaException(ErrorCode.GACHA_DAILY_LIMIT);
        }

        // Validate: at least 1 ingredient required
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new GachaException(ErrorCode.GACHA_NO_INGREDIENTS);
        }

        // Build ingredient/sauce strings for AI prompt
        List<String> ingredientNames = request.getIngredients().stream()
                .map(GachaDto.IngredientInput::getName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .collect(Collectors.toList());
        String ingredientStr = String.join(", ", ingredientNames);
        String sauceStr = "";
        if (request.getSauces() != null && !request.getSauces().isEmpty()) {
            sauceStr = request.getSauces().stream()
                    .map(GachaDto.IngredientInput::getName)
                    .filter(n -> n != null && !n.trim().isEmpty())
                    .collect(Collectors.joining(", "));
        }

        String purpose = request.getPurpose() != null ? request.getPurpose() : "속세의맛";
        String cuisine = request.getCuisine() != null ? request.getCuisine() : "상관없음";
        String difficulty = request.getDifficulty() != null ? request.getDifficulty() : "상관없음";
        String aiDifficulty = DIFFICULTY_AI_MAP.getOrDefault(difficulty, "ANY");

        // Call Gemini with retry
        JsonNode recipeJson = null;
        Exception lastException = null;

        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                String rawJson = geminiClient.generateRecipeJson(
                        ingredientStr, sauceStr, purpose, cuisine, aiDifficulty);
                recipeJson = objectMapper.readTree(rawJson);
                if (recipeJson.has("recipe")) {
                    break;
                }
                recipeJson = null;
            } catch (Exception e) {
                lastException = e;
                log.warn("Gemini attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt < MAX_RETRY) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }

        if (recipeJson == null || !recipeJson.has("recipe")) {
            log.error("All Gemini attempts failed", lastException);
            throw new GachaException(ErrorCode.GACHA_AI_FAILURE);
        }

        // Parse AI response
        JsonNode recipe = recipeJson.get("recipe");
        String title = recipe.path("title").asText("AI 레시피");
        String summary = recipe.path("summary").asText("");
        String aiDifficultyResult = recipe.path("difficulty").asText("MEDIUM");
        int estimatedTime = recipe.path("estimatedTimeMinutes").asInt(30);

        // Generate image URL
        Integer cuisineId = CUISINE_MAP.getOrDefault(cuisine, 1);
        String cuisineStyle = CUISINE_STYLE_MAP.getOrDefault(cuisineId, "Any");
        String imageUrl = imageGenerationClient.generateImageUrl(title, cuisineStyle);

        // Map difficulty/purpose/cuisine to DB IDs
        Integer difficultyId = DIFFICULTY_RESULT_MAP.getOrDefault(aiDifficultyResult, 2);
        Integer purposeId = PURPOSE_MAP.getOrDefault(purpose, 1);

        // Build RecipeEntity for saving
        RecipeEntity recipeEntity = RecipeEntity.builder()
                .userId(userId)
                .title(title)
                .titleImage(imageUrl)
                .description(summary)
                .cookingTime(estimatedTime)
                .difficultyId(difficultyId)
                .purposeId(purposeId)
                .cuisineId(cuisineId)
                .isAiGenerated(true)
                .isPublic(false)
                .build();

        // Parse and normalize ingredients
        List<RecipeEntity.RecipeIngredient> dbIngredients = new ArrayList<>();
        JsonNode ingredientsNode = recipe.path("ingredients");
        if (ingredientsNode.isArray()) {
            for (JsonNode ing : ingredientsNode) {
                String ingName = ing.path("name").asText("");
                BigDecimal amount = BigDecimal.valueOf(ing.path("amount").asDouble(0));
                String unit = ing.path("unit").asText("");
                String normalizedName = ingredientService.normalize(ingName);

                dbIngredients.add(RecipeEntity.RecipeIngredient.builder()
                        .name(ingName)
                        .normalizedName(normalizedName)
                        .quantity(amount)
                        .unit(unit)
                        .isAiGenerated(true)
                        .build());
            }
        }
        recipeEntity.setIngredients(dbIngredients);

        // Parse steps
        List<RecipeEntity.RecipeStep> dbSteps = new ArrayList<>();
        JsonNode stepsNode = recipe.path("steps");
        if (stepsNode.isArray()) {
            for (int i = 0; i < stepsNode.size(); i++) {
                dbSteps.add(RecipeEntity.RecipeStep.builder()
                        .stepOrder(i + 1)
                        .content(stepsNode.get(i).asText(""))
                        .build());
            }
        }
        recipeEntity.setSteps(dbSteps);

        // Parse nutrients
        List<RecipeEntity.RecipeNutrient> dbNutrients = new ArrayList<>();
        int calories = recipe.path("calories").asInt(0);
        double protein = recipe.path("protein").asDouble(0);
        double fat = recipe.path("fat").asDouble(0);
        double carbs = recipe.path("carbs").asDouble(0);
        String nutrientNote = recipe.path("nutrient").asText("");

        if (calories > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName("calories").amount(BigDecimal.valueOf(calories)).unit("kcal").build());
        }
        if (protein > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName("protein").amount(BigDecimal.valueOf(protein)).unit("g").build());
        }
        if (fat > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName("fat").amount(BigDecimal.valueOf(fat)).unit("g").build());
        }
        if (carbs > 0) {
            dbNutrients.add(RecipeEntity.RecipeNutrient.builder()
                    .nutrientName("carbs").amount(BigDecimal.valueOf(carbs)).unit("g").build());
        }
        recipeEntity.setNutrients(dbNutrients);

        // Save to DB (only on successful parse)
        try {
            recipeService.saveFullRecipe(recipeEntity);
        } catch (Exception e) {
            log.error("Failed to save recipe to DB", e);
            throw new GachaException(ErrorCode.GACHA_SAVE_FAILURE);
        }

        // Build response DTO
        List<GachaDto.IngredientResult> responseIngredients = dbIngredients.stream()
                .map(ing -> GachaDto.IngredientResult.builder()
                        .name(ing.getName())
                        .amount(ing.getQuantity())
                        .unit(ing.getUnit())
                        .build())
                .collect(Collectors.toList());

        List<String> responseSteps = dbSteps.stream()
                .map(RecipeEntity.RecipeStep::getContent)
                .collect(Collectors.toList());

        GachaDto.NutrientResult nutrientResult = GachaDto.NutrientResult.builder()
                .calories(calories)
                .protein(BigDecimal.valueOf(protein))
                .fat(BigDecimal.valueOf(fat))
                .carbs(BigDecimal.valueOf(carbs))
                .note(nutrientNote)
                .build();

        String diffLabel = DIFFICULTY_LABEL_MAP.getOrDefault(aiDifficultyResult, "중");

        GachaDto.RecipeResult result = GachaDto.RecipeResult.builder()
                .id(recipeEntity.getId())
                .title(title)
                .titleImage(imageUrl)
                .summary(summary)
                .difficulty(aiDifficultyResult)
                .difficultyLabel(diffLabel)
                .cookingTime(estimatedTime)
                .purpose(purpose)
                .cuisine(cuisine)
                .ingredients(responseIngredients)
                .steps(responseSteps)
                .nutrients(nutrientResult)
                .build();

        return GachaDto.GenerateResponse.builder()
                .recipe(result)
                .build();
    }
}
