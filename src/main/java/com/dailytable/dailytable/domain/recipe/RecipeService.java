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

    public void updateVisibility(Long id, Long userId, boolean isPublic) {
        // Check if recipe belongs to user
        RecipeEntity recipe = recipeRepository.findById(id);
        if (recipe == null || !recipe.getUserId().equals(userId)) {
            throw new RuntimeException("レシピが見つからないか、権限がありません。");
        }
        recipeRepository.updatePublicStatus(id, isPublic);
    }
}
