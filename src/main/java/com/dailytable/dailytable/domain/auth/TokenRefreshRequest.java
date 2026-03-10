package com.dailytable.dailytable.domain.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {

    private String refreshToken;
}
