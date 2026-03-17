package com.dailytable.dailytable.domain.recipe;

import com.dailytable.dailytable.domain.recipe.dto.RecipeRankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.dailytable.dailytable.domain.ingredient.IngredientDto;
import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;
import com.dailytable.dailytable.domain.recipe.dto.RecipeNutrientDto;
import com.dailytable.dailytable.domain.recipe.dto.RecipeStepDto;

import java.util.List;

@Mapper 
public interface RecipeMapper {

	void insertRecipe(RecipeEntity recipe);

	void insertRecipeIngredient(RecipeEntity.RecipeIngredient ingredient);

	void insertRecipeStep(RecipeEntity.RecipeStep step);

	void insertRecipeNutrient(RecipeEntity.RecipeNutrient nutrient);

	RecipeDetailDto findById(@Param("id") Long id);

	List<IngredientDto> findIngredientsByRecipeId(@Param("recipeId") Long recipeId);

	List<RecipeStepDto> findStepsByRecipeId(@Param("recipeId") Long recipeId);

	List<RecipeNutrientDto> findNutrientsByRecipeId(Long recipeId);

	List<RecipeRankingDto> findPublicRecipes();

	void updatePublicStatus(@Param("id") Long id, @Param("isPublic") boolean isPublic);

	Long findLikeId(@Param("userId") Long userId,
			@Param("recipeId") Long recipeId);

	boolean existsViewLog(@Param("userId") Long userId,
			@Param("recipeId") Long recipeId);

	void insertViewLog(@Param("userId") Long userId,
			@Param("recipeId") Long recipeId);

	void increaseViewCount(@Param("recipeId") Long recipeId);

	void deleteRecipe(@Param("id") Long id, @Param("userId") Long userId);

}
