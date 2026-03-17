package com.dailytable.dailytable.domain.comment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dailytable.dailytable.domain.comment.dto.CommentCreateDto;
import com.dailytable.dailytable.domain.comment.dto.CommentEditDto;
import com.dailytable.dailytable.domain.comment.dto.CommentResponseDto;
import com.dailytable.dailytable.global.util.TimeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentMapper commentMapper;

	@Transactional
	public void insertComment(Long recipeId, Long userId, String content) {

		CommentCreateDto commentCreateDto = new CommentCreateDto();
		commentCreateDto.setUserId(userId);
		commentCreateDto.setContent(content);

		commentMapper.insertComment(recipeId, commentCreateDto);
		commentMapper.increaseCommentCount(recipeId);
	}

	public List<CommentResponseDto> getRecipeComments(Long recipeId, int page) {
		int size = 10;
		int offset = (page - 1) * size;

		List<CommentResponseDto> comments = commentMapper.selectComments(recipeId, size, offset);

		for (CommentResponseDto comment : comments) {
			comment.setCreatedAtFormatted(TimeUtil.formatRelativeTime(comment.getCreatedAt()));
		}

		return comments;
	}

	@Transactional
	public void deleteComment(Long commentId, Long recipeId, Long userId) {

		int deleted = commentMapper.deleteComment(commentId, userId);
		if (deleted == 1) {
			commentMapper.decreaseCommentCount(recipeId);
		}
	}

	public void editComment(CommentEditDto commentEditDto, Long userId) {

		commentMapper.updateComment(commentEditDto, userId);

	}

}
