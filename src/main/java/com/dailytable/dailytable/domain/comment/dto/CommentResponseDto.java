package com.dailytable.dailytable.domain.comment.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponseDto {
    private Long commentId;
    private Long userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private String createdAtFormatted;
}