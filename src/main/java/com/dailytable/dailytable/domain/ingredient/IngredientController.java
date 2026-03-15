package com.dailytable.dailytable.domain.ingredient;

import com.dailytable.dailytable.global.response.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 재료/소스 관리 - 사용자의 냉장고 재료와 조미료 관리
 * 재료 등록 시 3단계 정규화(전처리 → Alias DB → AI)를 통해 표준화된 이름으로 저장
 * type: 1=재료(冷蔵庫の食材), 2=소스(調味料)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * [재료/소스 목록 조회] 현재 로그인한 사용자의 재료 또는 소스 목록 반환
     * JWT 토큰에서 userId 추출 → 해당 사용자의 재료만 조회
     * type 파라미터: 1=재료, 2=소스 (null이면 전체)
     * Soft Delete: deleted_at IS NULL인 레코드만 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<IngredientRepository.UserIngredient>>> getMyIngredients(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer type) {
        log.info("Getting ingredients for user: {}, type: {}", userId, type);
        List<IngredientRepository.UserIngredient> ingredients = ingredientService.getMyIngredients(userId, type);
        return ResponseEntity.ok(ApiResponse.success(ingredients));
    }

    /**
     * [재료/소스 등록] 새로운 재료 또는 소스를 사용자 냉장고에 추가
     * 3단계 정규화 과정: 1) 코드 전처리 → 2) Alias DB 조회 → 3) AI 정규화
     * ON DUPLICATE KEY UPDATE: 중복 시 수량 업데이트
     * type: 1=재료, 2=소스
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IngredientRepository.UserIngredient>> addIngredient(
            @AuthenticationPrincipal Long userId,
            @RequestBody @jakarta.validation.Valid IngredientRequest request) {
        log.info("Adding ingredient for user: {}, name: {}", userId, request.getName());
        IngredientRepository.UserIngredient ingredient = ingredientService.addIngredient(
                userId,
                request.getName(),
                request.getQuantity(),
                request.getUnit(),
                request.getType()
        );
        return ResponseEntity.ok(ApiResponse.success("食材が追加されました!", ingredient));
    }

    /**
     * [재료/소스 삭제] 사용자의 재료를 Soft Delete로 삭제
     * 실제로 DB에서 삭제하지 않고 deleted_at 필드에 현재 시간을 기록
     * 본인의 재료만 삭제 가능 (userId 검증)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIngredient(
            @AuthenticationPrincipal Long userId,
            @PathVariable @Positive(message = "IDは正の数である必要があります") Long id) {
        log.info("Deleting ingredient: {} for user: {}", id, userId);
        boolean deleted = ingredientService.deleteIngredient(userId, id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("食材が削除されました!", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("食材が見つかりません。"));
        }
    }
}
