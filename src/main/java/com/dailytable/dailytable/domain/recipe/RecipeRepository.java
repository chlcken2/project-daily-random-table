package com.dailytable.dailytable.domain.recipe;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecipeRepository {

    void insertRecipe(RecipeEntity recipe);

    void insertRecipeIngredient(RecipeEntity.RecipeIngredient ingredient);

    void insertRecipeStep(RecipeEntity.RecipeStep step);

    void insertRecipeNutrient(RecipeEntity.RecipeNutrient nutrient);

    RecipeEntity findById(@Param("id") Long id);

    List<RecipeEntity.RecipeIngredient> findIngredientsByRecipeId(@Param("recipeId") Long recipeId);

    List<RecipeEntity.RecipeStep> findStepsByRecipeId(@Param("recipeId") Long recipeId);

    List<RecipeEntity.RecipeNutrient> findNutrientsByRecipeId(@Param("recipeId") Long recipeId);

    void updatePublicStatus(@Param("id") Long id, @Param("isPublic") boolean isPublic);

    // For RecipeController - public recipes
    List<RecipeDto> findPublicRecipes();

    List<RecipeDto> findAllPublicRecipes();

    List<RecipeDto> findByUserId(@Param("userId") Long userId);

    List<RecipeDto> findLikedRecipesByUserId(@Param("userId") Long userId);

    void incrementViewCount(@Param("id") Long id);

    // 공감 기능
    void toggleLike(@Param("recipeId") Long recipeId, @Param("userId") Long userId);

    void addLike(@Param("recipeId") Long recipeId, @Param("userId") Long userId);

    void updateLikeCount(@Param("recipeId") Long recipeId);

    // 덧글 기능
    void addComment(@Param("recipeId") Long recipeId, @Param("userId") Long userId, @Param("content") String content);

    void updateCommentCount(@Param("recipeId") Long recipeId);

    List<CommentDto> findCommentsByRecipeId(@Param("recipeId") Long recipeId);

    // 공감 취소
    void deleteLike(@Param("recipeId") Long recipeId, @Param("userId") Long userId);
}
