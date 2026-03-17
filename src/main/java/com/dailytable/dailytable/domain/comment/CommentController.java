package com.dailytable.dailytable.domain.comment;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dailytable.dailytable.domain.comment.dto.CommentCreateDto;
import com.dailytable.dailytable.domain.comment.dto.CommentEditDto;
import com.dailytable.dailytable.domain.comment.dto.CommentResponseDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class CommentController {

	private final CommentService commentService;

	@GetMapping("/{recipeId}/comments")
	@ResponseBody
	public List<CommentResponseDto> getComments(
			@PathVariable Long recipeId,
			@RequestParam(defaultValue = "1") int page) {  // page 쿼리 파라미터 받기

		return commentService.getRecipeComments(recipeId, page);
	}

	@PostMapping("/{recipeId}/comments")
	@ResponseBody
	public String insertComment(
			@PathVariable Long recipeId,
			@RequestBody CommentCreateDto commentCreateDto,
			@AuthenticationPrincipal Long userId) {


		String content = commentCreateDto.getContent();

		if (content == null || content.trim().isEmpty()) {
			return "コメントが入力できていません。";
		}

		commentService.insertComment(recipeId, userId, content);

		return "登録できました。";
	}

	@DeleteMapping("/{recipeId}/comments/{commentId}")
	@ResponseBody
	public String deleteComment(
			@PathVariable Long recipeId,
			@PathVariable Long commentId,
			@AuthenticationPrincipal Long userId) {


		commentService.deleteComment(commentId, recipeId, userId);

		return "削除できました。";
	}

	@PutMapping("/{recipeId}/comments/{commentId}")
	@ResponseBody
	public String editComment(
			@PathVariable Long recipeId,
			@PathVariable Long commentId,
			@RequestBody CommentEditDto commentEditDto,
			@AuthenticationPrincipal Long userId) {


		commentEditDto.setCommentId(commentId);

		commentService.editComment(commentEditDto, userId);

		return "編集できました。";
	}
	
	@GetMapping("/{recipeId}/comments/count")
	@ResponseBody
	public int getCommentCount(@PathVariable Long recipeId) {
	    return commentService.getCommentCount(recipeId);
	}
	
}