package com.dailytable.dailytable.domain.comment.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponseDto {
	private String nickname;
	private String content;
	private LocalDateTime createdAt;
	private String createdAtFormatted;
}
