package com.dailytable.dailytable.domain.auth;

import com.dailytable.dailytable.domain.auth.dto.request.TokenRefreshRequest;
import com.dailytable.dailytable.domain.auth.dto.request.UserLoginRequest;
import com.dailytable.dailytable.domain.auth.dto.request.UserSignupRequest;
import com.dailytable.dailytable.domain.auth.dto.response.AuthResponse;
import com.dailytable.dailytable.global.response.ApiResponse;
import com.dailytable.dailytable.global.util.AuthHeaderUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("가입 완료", null));
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
                                    @CookieValue(value = "refreshToken", required = false) String refreshToken,
                                    HttpServletResponse response) {
    	String token = refreshToken != null && !refreshToken.isBlank() ? refreshToken : AuthHeaderUtils.extractBearerToken(authHeader);
        authService.logout(token);

        // Clear cookies
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
    }

   
}
