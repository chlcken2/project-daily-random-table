package com.dailytable.dailytable.domain.recipe;


import com.dailytable.dailytable.domain.recipe.dto.RecipeRankingDto;
import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class RecipeController {

	private final RecipeService recipeService;

	@GetMapping("/{id}")
	public String getRecipeDetail(
			@PathVariable("id") Long id,
			@AuthenticationPrincipal Long userId,
			Model model
			) {
		RecipeDetailDto recipe = recipeService.getRecipeDetail(id, userId);
		model.addAttribute("recipe", recipe);
		return "recipe-detail";
	}
    /**
     * 레시피 공개/비공개 상태 변경
     * 가챠로 생성한 레시피를 'みんなの食卓'에 공개하거나 비공개로 설정
     * 본인 레시피만 수정 가능 (userId 검증)
     */
    @PatchMapping("/{id}/visibility")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> updateVisibility(
            @PathVariable Long id,
            @RequestParam boolean isPublic,
            @AuthenticationPrincipal Long userId) {
        log.info("PATCH /recipes/{}/visibility, isPublic: {}, userId: {}", id, isPublic, userId);
        recipeService.updateVisibility(id, userId, isPublic);
        String msg = isPublic ? "公開設定にしました!" : "非公開設定にしました!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
	/**
	 * [모두의 식탁] 공개 레시피 목록 조회
	 * type 파라미터에 따라 다른 목록 반환:
	 * - publicAll: 모든 공개 레시피 (홈 화면 'みんなの食卓' 탭)
	 *
	 * JWT 토큰에서 userId 추출 → 해당 사용자의 데이터만 필터링
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<RecipeRankingDto>>> getRecipes(
			@AuthenticationPrincipal Long userId,
			@RequestParam(required = false) String type) {
		log.info("GET /recipes, userId: {}, type: {}", userId, type);

		List<RecipeRankingDto> recipes = recipeService.getPublicRecipes();
		return ResponseEntity.ok(ApiResponse.success(recipes));
	}

}