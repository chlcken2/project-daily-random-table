package com.dailytable.dailytable.domain.comment;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dailytable.dailytable.domain.comment.dto.CommentCreateDto;
import com.dailytable.dailytable.domain.comment.dto.CommentEditDto;
import com.dailytable.dailytable.domain.comment.dto.CommentResponseDto;

public interface CommentMapper {

	void insertComment(
			@Param("recipeId") Long recipeId,
			@Param("commentCreateDto") CommentCreateDto commentCreateDto
			);


	List<CommentResponseDto> selectComments(Long recipeId);

	void increaseCommentCount(Long recipeId);

	int deleteComment(
		    @Param("commentId") Long commentId,
		    @Param("userId") Long userId
		);

	void decreaseCommentCount(Long recipeId);
	
	void updateComment(
	        @Param("commentEditDto") CommentEditDto dto,
	        @Param("userId") Long userId
	);

}
