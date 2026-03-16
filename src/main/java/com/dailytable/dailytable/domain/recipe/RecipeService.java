package com.dailytable.dailytable.domain.recipe;

import com.dailytable.dailytable.domain.recipe.dto.RecipeRankingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import com.dailytable.dailytable.global.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

//RecipeService
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

	private final RecipeMapper recipeMapper;

    @Transactional
    public void saveFullRecipe(RecipeEntity recipe) {
        // Insert main recipe
        recipeMapper.insertRecipe(recipe);
        Long recipeId = recipe.getId();

        // Insert ingredients
        if (recipe.getIngredients() != null) {
            for (RecipeEntity.RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipeId(recipeId);
                recipeMapper.insertRecipeIngredient(ingredient);
            }
        }

        // Insert steps
        if (recipe.getSteps() != null) {
            for (RecipeEntity.RecipeStep step : recipe.getSteps()) {
                step.setRecipeId(recipeId);
                recipeMapper.insertRecipeStep(step);
            }
        }

        // Insert nutrients
        if (recipe.getNutrients() != null) {
            for (RecipeEntity.RecipeNutrient nutrient : recipe.getNutrients()) {
                nutrient.setRecipeId(recipeId);
                recipeMapper.insertRecipeNutrient(nutrient);
            }
        }

    }

	@Transactional
	public RecipeDetailDto getRecipeDetail(Long recipeId, Long userId) {
		// 1 기본 레시피 조회
        RecipeDetailDto recipe = recipeMapper.findById(recipeId);
		if (recipe == null) {
			throw new BaseException(ErrorCode.RECIPE_NOT_FOUND);
		}

		// 2 조회수 처리
		if (userId != null) {

			boolean hasViewLog = recipeMapper.existsViewLog(userId, recipeId);

			if (!hasViewLog) {
				recipeMapper.insertViewLog(userId, recipeId);
				recipeMapper.increaseViewCount(recipeId);
			}

			//3 좋아요 여부
			Long likeId = recipeMapper.findLikeId(userId, recipeId);
			recipe.setLiked(likeId != null);
		}

		// 4 steps 조회
		recipe.setSteps(
				recipeMapper.findStepsByRecipeId(recipeId)
				);

		// 5 ingredients 조회
		recipe.setIngredients(
				recipeMapper.findIngredientsByRecipeId(recipeId)
				);

		// 6 nutrients 조회
		recipe.setNutrients(
				recipeMapper.findNutrientsByRecipeId(recipeId)
				);

		// 6 시간 변환
		recipe.setCreatedAtFormatted(
				TimeUtil.formatRelativeTime(recipe.getCreatedAt())
				);

		return recipe;
	}

    // note: 추후 기능 체크할 것
    public void updateVisibility(Long id, Long userId, boolean isPublic) {
        recipeMapper.updatePublicStatus(id, isPublic);
    }

	// Methods for RecipeController
	public List<RecipeRankingDto> getPublicRecipes() {
		return recipeMapper.findPublicRecipes();
	}
}
