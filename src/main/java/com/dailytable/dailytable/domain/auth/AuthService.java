package com.dailytable.dailytable.domain.auth;

import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void signup(UserSignupRequest dto) {
        String email = dto.getEmail() != null ? dto.getEmail().trim() : "";
        String nickname = dto.getNickname() != null ? dto.getNickname().trim() : "";
        String password = dto.getPassword() != null ? dto.getPassword().trim() : "";
        String confirmPassword = dto.getConfirmPassword() != null ? dto.getConfirmPassword().trim() : "";

        if (email.isEmpty()) throw new BaseException(ErrorCode.INVALID_REQUEST);
        if (nickname.isEmpty()) throw new BaseException(ErrorCode.INVALID_REQUEST);
        if (password.isEmpty()) throw new BaseException(ErrorCode.INVALID_REQUEST);
        if (confirmPassword.isEmpty()) throw new BaseException(ErrorCode.INVALID_REQUEST);
        if (!password.equals(confirmPassword)) throw new BaseException(ErrorCode.PASSWORD_MISMATCH);

        if (authMapper.existsByEmail(email)) throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        if (authMapper.existsByNickname(nickname)) throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);

        String hashedPassword = passwordEncoder.encode(password);

        UserEntity user = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .password(hashedPassword)
                .role("user")
                .build();

        authMapper.insert(user);
    }
}
