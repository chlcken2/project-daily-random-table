package com.dailytable.dailytable.domain.auth;

import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import com.dailytable.dailytable.global.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final int REFRESH_TOKEN_VALID_DAYS = 7;

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;

    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider, RefreshTokenMapper refreshTokenMapper) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshTokenMapper = refreshTokenMapper;
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

    public AuthResponse login(UserLoginRequest dto) {
        String email = dto.getEmail() != null ? dto.getEmail().trim() : "";
        String password = dto.getPassword() != null ? dto.getPassword().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_LOGIN);
        }

        UserEntity user = authMapper.selectByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_LOGIN);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALID_DAYS);

        RefreshToken rt = RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(expiresAt)
                .build();
        refreshTokenMapper.insert(rt);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshAccessToken(TokenRefreshRequest dto) {
        String refreshToken = dto.getRefreshToken() != null ? dto.getRefreshToken().trim() : "";
        if (refreshToken.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }

        RefreshToken rt = refreshTokenMapper.selectByTokenAndNotExpired(
                refreshToken,
                LocalDateTime.now()
        );
        if (rt == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }

        String newAccessToken = jwtProvider.createAccessToken(rt.getUserId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokenMapper.deleteByToken(refreshToken);
    }
}
