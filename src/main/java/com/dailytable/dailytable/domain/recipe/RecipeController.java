package com.dailytable.dailytable.domain.recipe;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dailytable.dailytable.domain.recipe.dto.RecipeDetailDto;

import lombok.RequiredArgsConstructor;

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
}