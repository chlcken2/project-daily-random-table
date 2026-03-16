package com.dailytable.dailytable.domain.recipe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public RecipeEntity saveFullRecipe(RecipeEntity recipe) {
        // Insert main recipe
        recipeRepository.insertRecipe(recipe);
        Long recipeId = recipe.getId();

        // Insert ingredients
        if (recipe.getIngredients() != null) {
            for (RecipeEntity.RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipeId(recipeId);
                recipeRepository.insertRecipeIngredient(ingredient);
            }
        }

        // Insert steps
        if (recipe.getSteps() != null) {
            for (RecipeEntity.RecipeStep step : recipe.getSteps()) {
                step.setRecipeId(recipeId);
                recipeRepository.insertRecipeStep(step);
            }
        }

        // Insert nutrients
        if (recipe.getNutrients() != null) {
            for (RecipeEntity.RecipeNutrient nutrient : recipe.getNutrients()) {
                nutrient.setRecipeId(recipeId);
                recipeRepository.insertRecipeNutrient(nutrient);
            }
        }

        return recipe;
    }

    public RecipeEntity getRecipeDetail(Long id) {
        RecipeEntity recipe = recipeRepository.findById(id);
        if (recipe == null) return null;

        // 상세페이지 진입 시 조회수 증가 (API, Web 공통)
        recipeRepository.incrementViewCount(id);

        recipe.setIngredients(recipeRepository.findIngredientsByRecipeId(id));
        recipe.setSteps(recipeRepository.findStepsByRecipeId(id));
        recipe.setNutrients(recipeRepository.findNutrientsByRecipeId(id));

        return recipe;
    }

    public void updatePublicStatus(Long id, boolean isPublic) {
        recipeRepository.updatePublicStatus(id, isPublic);
    }

    // Methods for RecipeController
    public List<RecipeDto> getPublicRecipes() {
        return recipeRepository.findPublicRecipes();
    }

    public List<RecipeDto> getAllPublicRecipes() {
        return recipeRepository.findAllPublicRecipes();
    }

    public List<RecipeDto> getMyRecipes(Long userId) {
        return recipeRepository.findByUserId(userId);
    }

    public List<RecipeDto> getLikedRecipes(Long userId) {
        return recipeRepository.findLikedRecipesByUserId(userId);
    }

    public RecipeDetailDto getRecipeDetail(Long id, Long userId) {
        RecipeEntity recipe = getRecipeDetail(id);
        if (recipe == null) return null;
        
        // Convert to RecipeDetailDto
        return convertToDetailDto(recipe, userId);
    }

    public void updateVisibility(Long id, Long userId, boolean isPublic) {
        // Check if recipe belongs to user
        RecipeEntity recipe = recipeRepository.findById(id);
        if (recipe == null || !recipe.getUserId().equals(userId)) {
            throw new RuntimeException("レシピが見つからないか、権限がありません。");
        }
        recipeRepository.updatePublicStatus(id, isPublic);
    }

    private RecipeDetailDto convertToDetailDto(RecipeEntity recipe, Long userId) {
        // Conversion logic here - simplified version
        RecipeDetailDto dto = new RecipeDetailDto();
        dto.setId(recipe.getId());
        dto.setUserId(recipe.getUserId());
        dto.setTitle(recipe.getTitle());
        dto.setTitleImage(recipe.getTitleImage());
        dto.setDescription(recipe.getDescription());
        dto.setCookingTime(recipe.getCookingTime());
        
        // Map difficultyId to Japanese label safely (bypassing DB encoding issues)
        String label = "中";
        if (recipe.getDifficultyId() != null) {
            if (recipe.getDifficultyId() == 1) label = "低";
            else if (recipe.getDifficultyId() == 3) label = "高";
        }
        dto.setDifficultyLabel(label);
        
        dto.setAiGenerated(recipe.getIsAiGenerated() != null ? recipe.getIsAiGenerated() : false);
        dto.setPublic(recipe.getIsPublic() != null ? recipe.getIsPublic() : false);
        dto.setViewCount(recipe.getViewCount());
        dto.setCommentCount(recipe.getCommentCount());
        dto.setLikeCount(recipe.getLikeCount());
        dto.setPopularityScore(recipe.getPopularityScore());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());
        return dto;
    }

    // 공감 기능
    public void toggleLike(Long recipeId, Long userId) {
        // 먼저 삭제 시도
        recipeRepository.toggleLike(recipeId, userId);
        // 삭제된 행이 없으면 (0이면) 추가
        recipeRepository.addLike(recipeId, userId);
        recipeRepository.updateLikeCount(recipeId);
    }

    // 덧글 작성
    public void addComment(Long recipeId, Long userId, String content) {
        recipeRepository.addComment(recipeId, userId, content);
        recipeRepository.updateCommentCount(recipeId);
    }

    // 덧글 목록 조회
    public List<CommentDto> getComments(Long recipeId) {
        return recipeRepository.findCommentsByRecipeId(recipeId);
    }

    // 공감 취소
    public void cancelLike(Long recipeId, Long userId) {
        recipeRepository.deleteLike(recipeId, userId);
        recipeRepository.updateLikeCount(recipeId);
    }
}
