package com.dailytable.dailytable.domain.ingredient;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class IngredientDto {
    private Long id;
    private Long recipeId;
    private String name;
    private String normalizedName;
    private BigDecimal quantity;
    private String unit;
    private int isAiGenerated;
}
