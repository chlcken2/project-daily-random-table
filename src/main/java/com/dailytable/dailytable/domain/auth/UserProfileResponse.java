package com.dailytable.dailytable.domain.auth;

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


}
