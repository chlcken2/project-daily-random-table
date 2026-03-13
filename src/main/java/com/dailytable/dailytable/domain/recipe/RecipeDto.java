package com.dailytable.dailytable.domain.recipe;

import lombok.Data;

@Data
public class RecipeDto {
    private Long id;
    private String title;
    private String titleImage;
    private String description;
    private int cookingTime;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private boolean isPublic;
    private boolean isAiGenerated;
    private String authorNickname;
    private String difficultyName;
}
