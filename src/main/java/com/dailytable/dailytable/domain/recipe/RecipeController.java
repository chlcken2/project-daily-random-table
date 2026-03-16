package com.dailytable.dailytable.domain.recipe;


import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;

import lombok.RequiredArgsConstructor;

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
        log.info("PATCH /api/recipes/{}/visibility, isPublic: {}, userId: {}", id, isPublic, userId);
        recipeService.updateVisibility(id, userId, isPublic);
        String msg = isPublic ? "公開設定にしました!" : "非公開設定にしました!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
}