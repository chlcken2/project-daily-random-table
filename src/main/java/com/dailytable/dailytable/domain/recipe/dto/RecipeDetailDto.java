package com.dailytable.dailytable.domain.recipe.dto;

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
	private String nickname;
	private int cookingTime;
	private String difficultyLabel;
	private String purpose;
	private String cuisine;
	private boolean isPublic;
	private int viewCount;
	private int commentCount;
	private int likeCount;
	private boolean isLiked;
	private List<RecipeStepDto> steps;
	private List<IngredientDto> ingredients;
	private List<RecipeNutrientDto> nutrients;
	private LocalDateTime createdAt;
	private String createdAtFormatted;
	
}