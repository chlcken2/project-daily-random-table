package com.dailytable.dailytable.domain.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequest {

    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 6, max = 16, message = "비밀번호는 6자 이상 16자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
    private String confirmPassword;
}
