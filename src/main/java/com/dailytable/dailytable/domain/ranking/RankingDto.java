package com.dailytable.dailytable.domain.ranking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RankingDto {
    private Long id;
    private String title;
    private String titleImage;
    private String nickname;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private double popularityScore;
    private String difficultyLabel;
    private int cookingTime;
    private LocalDateTime createdAt;
}
