package com.dailytable.dailytable.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String email;
    private String nickname;
    private String role;
    private int myRecipeCount;         // 내가 만든 레시피 개수 (0부터 증가만 하므로 int)
    private int receivedLikeCount;     // 내가 받은 좋아요 수
    private int totalGachaCount;       // 가챠 총 횟수
}
