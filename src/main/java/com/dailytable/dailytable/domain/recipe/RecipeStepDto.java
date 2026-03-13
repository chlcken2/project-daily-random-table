package com.dailytable.dailytable.domain.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeStepDto {
    private Long id;
    private int stepOrder;
    private String content;
}
