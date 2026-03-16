package com.dailytable.dailytable.domain.recipe.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RecipeNutrientDto {

	private long id;
	private long recipeId;
	private String nutrientName;
	private BigDecimal amount;
	private String unit;
}
