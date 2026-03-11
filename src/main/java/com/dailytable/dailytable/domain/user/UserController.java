package com.dailytable.dailytable.domain.user;


import com.dailytable.dailytable.domain.auth.AuthService;
import com.dailytable.dailytable.domain.auth.UserProfileResponse;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Long userId) {
        if(userId == null) {
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED));
        }
        UserProfileResponse profile = authService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회 성공",profile));


    }
}
