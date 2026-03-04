package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.domain.recipe.RecipeEntity;
import com.dailytable.dailytable.domain.recipe.RecipeService;
import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/gacha")
public class GachaController {

    // Dummy user ID for testing (until auth is integrated)
    private static final Long DUMMY_USER_ID = 1L;

    private final GachaService gachaService;
    private final RecipeService recipeService;

    @GetMapping("/home")
    public String getGachaHome(Model model) {
        GachaDto.DailyCountResponse dailyCount = gachaService.getDailyCount(DUMMY_USER_ID);
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
    public ResponseEntity<ApiResponse<GachaDto.GenerateResponse>> generateRecipe(
            @RequestBody GachaDto.GenerateRequest request) {
        GachaDto.GenerateResponse response = gachaService.generate(request, DUMMY_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("레시피가 생성되었습니다!", response));
    }

    /**
     * 오늘 남은 가챠 횟수 조회
     * 성공: ApiResponse { success:true, message:"OK", data: { count:1, max:3, canGenerate:true } }
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<ApiResponse<GachaDto.DailyCountResponse>> getDailyCount() {
        return ResponseEntity.ok(ApiResponse.success(gachaService.getDailyCount(DUMMY_USER_ID)));
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
        String msg = isPublic ? "모두의 식탁에 등록되었습니다!" : "나만의 식탁에 등록되었습니다!";
        return ResponseEntity.ok(ApiResponse.success(msg, null));
    }

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
