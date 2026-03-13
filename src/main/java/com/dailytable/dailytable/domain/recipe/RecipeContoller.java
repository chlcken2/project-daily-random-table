package com.dailytable.dailytable.domain.recipe;

import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 모두의 식탁 - 공개 레시피 API
 * 모든 사용자가 공개한 레시피 목록을 조회하는 기능
 * JWT 인증을 통해 사용자별로 내 레시피/좋아요한 레시피도 조회 가능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
public class RecipeContoller {

    private final RecipeService recipeService;

    /**
     * [모두의 식탁] 공개 레시피 목록 조회
     * type 파라미터에 따라 다른 목록 반환:
     * - publicAll: 모든 공개 레시피 (홈 화면 'みんなの食卓' 탭)
     * - my: 내가 작성한 레시피 (마이페이지)
     * - liked: 내가 좋아요한 레시피 (마이페이지)
     * 
     * JWT 토큰에서 userId 추출 → 해당 사용자의 데이터만 필터링
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipeDto>>> getRecipes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String type) {
        log.info("GET /api/recipes, userId: {}, type: {}", userId, type);

        List<RecipeDto> recipes;
        if ("publicAll".equals(type)) {
            recipes = recipeService.getPublicRecipes();
        } else if ("my".equals(type)) {
            recipes = recipeService.getMyRecipes(userId);
        } else if ("liked".equals(type)) {
            recipes = recipeService.getLikedRecipes(userId);
        } else {
            recipes = recipeService.getAllPublicRecipes();
        }
        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    /**
     * 레시피 상세 조회
     * path variable로 레시피 ID 받아서 상세 정보 반환
     * RecipeDetailDto: 재료, 조리步骤, 영양정보 포함
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecipeDetailDto>> getRecipeDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        log.info("GET /api/recipes/{}, userId: {}", id, userId);
        RecipeDetailDto recipe = recipeService.getRecipeDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(recipe));
    }

    /**
     * 레시피 공개/비공개 상태 변경
     * 가챠로 생성한 레시피를 'みんなの食卓'에 공개하거나 비공개로 설정
     * 본인 레시피만 수정 가능 (userId 검증)
     */
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ApiResponse<Void>> updateVisibility(
            @PathVariable Long id,
            @RequestParam boolean isPublic,
            @AuthenticationPrincipal Long userId) {
        log.info("PATCH /api/recipes/{}/visibility, isPublic: {}, userId: {}", id, isPublic, userId);
        recipeService.updateVisibility(id, userId, isPublic);
        String msg = isPublic ? "公開設定にしました!" : "非公開設定にしました!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
}
