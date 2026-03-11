package com.dailytable.dailytable.domain.comment.dto;

import lombok.Data;

// 댓글 작성 수정 요청을 위한 DTO
@Data
public class CommentEditDto {
	private long commentId;
	private String content;
}
