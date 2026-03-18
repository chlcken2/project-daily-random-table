package com.dailytable.dailytable.domain.recipe;

import java.time.LocalDateTime;
import java.util.List;

import com.dailytable.dailytable.domain.ingredient.IngredientDto;

import lombok.Data;

@Data
public class RecipeDetailDto {
	private Long id;
	private Long userId;
	private String title;
	private String titleImage;
	private String description;
	private int cookingTime;
	private String difficultyLabel;
	private String purpose;
	private String createdAtFormatted;
	private String cuisine;
	private boolean isAiGenerated;
	private boolean isPublic;
	private int viewCount;
	private int commentCount;
	private int likeCount;
	private int popularityScore;
	private boolean isLiked;
	private List<RecipeStepDto> steps;
	private List<IngredientDto> ingrediens
	private List<RecipeNutrientDto> nutrients;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
}