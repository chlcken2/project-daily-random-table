package com.dailytable.dailytable.domain.recipe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import com.dailytable.dailytable.global.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;
//RecipeService
@Service
@Slf4j
public class RecipeService {

	private final RecipeMapper recipeMapper;

	public RecipeService(RecipeMapper recipeMapper) {
		this.recipeMapper = recipeMapper;
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
}
