package com.dailytable.dailytable.domain.recipe.dto;

import lombok.Data;

@Data
public class RecipeStepDto {
    private Long id;
    private Long recipeId;
    private int stepOrder;
    private String content;
}
