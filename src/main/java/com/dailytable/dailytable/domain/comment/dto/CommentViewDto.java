package com.dailytable.dailytable.domain.comment.dto;

import lombok.Data;

@Data
public class CommentViewDto {
    private Long commentId;
    private Long userId;
    private String nickname;
    private String content;
    private String createdAtFormatted;
}