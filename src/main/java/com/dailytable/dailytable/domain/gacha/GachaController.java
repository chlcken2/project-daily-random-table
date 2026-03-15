package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.recipe.RecipeEntity;
import com.dailytable.dailytable.domain.recipe.RecipeService;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/gacha")
public class GachaController {

    private final GachaService gachaService;
    private final RecipeService recipeService;

    @GetMapping("/home")
    public String getGachaHome(Model model, @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return "redirect:/login";
        }
        GachaDto.DailyCountResponse dailyCount = gachaService.getDailyCount(userId);
        model.addAttribute("dailyCount", dailyCount);
        return "recipe-create";
    }

    /**
     * 레시피 생성
     * 성공: ApiResponse { success:true, message:"레시피가 생성되었습니다!", data: { recipe:{...} } }
     * 실패: GlobalExceptionHandler → ApiResponse { success:false, message:"...", errorCode:"GACHA_*" }
     */
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> generateRecipe(
            @RequestBody GachaDto.GenerateRequest request, @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.fail(ErrorCode.UNAUTHORIZED));
        }
        GachaDto.GenerateResponse response = gachaService.generate(request, userId);
        return ResponseEntity.ok(ApiResponse.success("레시피가 생성되었습니다!", response));
    }

    /**
     * 오늘 남은 가챠 횟수 조회
     * 성공: ApiResponse { success:true, message:"OK", data: { count:1, max:3, canGenerate:true } }
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<ApiResponse<GachaDto.DailyCountResponse>> getDailyCount(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(gachaService.getDailyCount(userId)));
    }

    /**
     * 레시피 공개/비공개 저장
     * 성공: ApiResponse { success:true, message:"모두의 식탁에 등록되었습니다!" }
     * 실패: GlobalExceptionHandler → ApiResponse { success:false, message:"...", errorCode:"RECIPE_NOT_FOUND" }
     */
    @PostMapping("/publish/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> publishRecipe(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean isPublic) {
        recipeService.updatePublicStatus(id, isPublic);
        String msg = isPublic ? "みんなの食卓に登録されました!" : "私だけの食卓に登録されました!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

    // note: 선빈 개발 후 변경할 api
    @GetMapping("/recipe/{id}")
    public String getRecipeDetail(@PathVariable Long id, Model model) {
        RecipeEntity recipe = recipeService.getRecipeDetail(id);
        if (recipe == null) {
            return "redirect:/gacha/home";
        }
        model.addAttribute("recipe", recipe);
        return "recipe-detail";
    }
}
