package com.dailytable.dailytable.domain.auth;

import com.dailytable.dailytable.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("가입 완료", null));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신 성공", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        String token = refreshToken != null && !refreshToken.isBlank() ? refreshToken : extractRefreshTokenFromHeader(authHeader);
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

    private String extractRefreshTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring(7).trim();
    }
}
