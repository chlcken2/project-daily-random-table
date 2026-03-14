package com.dailytable.dailytable.domain.comment.dto;

import lombok.Data;

// 댓글 작성 요청을 위한 DTO
@Data
public class CommentCreateDto {
	private long userId;
	private String content;
}
