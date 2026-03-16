package com.dailytable.dailytable.domain.like;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class LikeController {

    private final LikeService likeService;
    //likecount
    @PostMapping("/{recipeId}/likes")
    @ResponseBody
    public String like(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long userId) {

        likeService.like(recipeId, userId);
        return "いいねしました。";
    }

    @DeleteMapping("/{recipeId}/likes")
    @ResponseBody
    public String unlike(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal Long userId) {

        likeService.unlike(recipeId, userId);
        return "いいねをキャンセルしました。";
    }
}