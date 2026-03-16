package com.dailytable.dailytable.domain.user.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMyRecipeResponse {
    private Long id;
    private String title;
    private String titleImage;
    private Boolean isPublic;
    private Integer likeCount;
    private Integer commentCount;
}
