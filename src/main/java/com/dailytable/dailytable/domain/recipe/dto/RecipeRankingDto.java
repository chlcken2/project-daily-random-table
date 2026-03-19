package com.dailytable.dailytable.domain.recipe.dto;

import lombok.Data;

@Data
public class RecipeRankingDto {
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
    // [추가] 기존에는 자바의 LocalDateTime을 직접 변환하려 했으나, SQL단에서 포맷팅된 날짜 문자열(YYYY-MM-DD)을 바로 받아오기 위해 추가된 필드임.
    private String createdAtStr;
}
