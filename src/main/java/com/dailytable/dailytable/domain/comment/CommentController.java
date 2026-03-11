package com.dailytable.dailytable.domain.comment;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dailytable.dailytable.domain.comment.dto.CommentEditDto;
import com.dailytable.dailytable.domain.comment.dto.CommentResponseDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/{recipeId}/comments")
	@ResponseBody
	public List<CommentResponseDto> getComments(
			@PathVariable Long recipeId
			) {

		return commentService.getRecipeComments(recipeId);
	}

	@PostMapping("/{recipeId}/comments")
	public String insertComment(
			@PathVariable Long recipeId,
			@RequestParam String content,
			HttpSession session) {

		Long userId = (Long) session.getAttribute("userId");

		if (userId == null) {
			return "redirect:/login";
		}

		if (content == null || content.trim().isEmpty()) {
			return "redirect:/recipes/" + recipeId;
		}

		commentService.insertComment(recipeId, userId, content);

		return "redirect:/recipes/" + recipeId;
	}

	@PostMapping("/{recipeId}/comments/{commentId}/delete")
	public String deleteComment(
			@PathVariable Long recipeId,
			@PathVariable Long commentId,
			HttpSession session) {

		Long userId = (Long) session.getAttribute("userId");

		if (userId == null) {
			return "redirect:/login";
		}

		commentService.deleteComment(commentId, recipeId, userId);

		return "redirect:/recipes/" + recipeId;
	}
	
	@PostMapping("/{recipeId}/comments/{commentId}/edit")
	public String editComment(
	        @PathVariable Long recipeId,
	        @PathVariable Long commentId,
	        @RequestParam String content,
	        HttpSession session) {

	    Long userId = (Long) session.getAttribute("userId");

	    if (userId == null) {
	        return "redirect:/login";
	    }

	    CommentEditDto dto = new CommentEditDto();
	    dto.setCommentId(commentId);
	    dto.setContent(content);

	    commentService.editComment(dto, userId);

	    return "redirect:/recipes/" + recipeId;
	}

}