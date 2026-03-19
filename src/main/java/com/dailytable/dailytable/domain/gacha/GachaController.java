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
            @RequestBody GachaDto.GenerateRequest request, @AuthenticationPrincipal Long userId) throws Exception {
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
     * 가챠 후  바로 보이는 화면에서 레시피 공개 버튼 존재, 누르면 모두의 식탁에 레시피 공개됨,나만의 식탁 누르면 비공개로 전환되며,
     마이페이지로 이동
     * 성공: ApiResponse { success:true, message:"모두의 식탁에 등록되었습니다!" }
     */
    @PostMapping("/publish/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> publishRecipe(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean isPublic,
            @AuthenticationPrincipal Long userId) {
        log.info("POST /gacha/publish/{}, isPublic: {}, userId: {}", id, isPublic, userId);
        recipeService.updateVisibility(id, userId, isPublic);
        String msg = isPublic ? "みんなの食卓に登録されました!" : "マイページに登録されました!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }
}
