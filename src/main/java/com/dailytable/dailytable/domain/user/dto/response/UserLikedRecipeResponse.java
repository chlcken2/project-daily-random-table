package com.dailytable.dailytable.domain.user.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLikedRecipeResponse {
    private Long id;
    private String title;
    private String titleImage;
    private Integer cookingTime;
    private String difficultyName;
    private Integer likeCount;
    private Integer commentCount;
}
