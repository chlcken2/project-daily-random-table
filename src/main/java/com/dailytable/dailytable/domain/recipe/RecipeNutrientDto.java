package com.dailytable.dailytable.domain.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeNutrientDto {
    private Long id;
    private String nutrientName;
    private double amount;
    private String unit;
}
