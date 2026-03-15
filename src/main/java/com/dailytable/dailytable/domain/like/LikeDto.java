package com.dailytable.dailytable.domain.like;

import lombok.Data;

@Data
public class LikeDto {
	private Long recipeId;
	private boolean isLiked;
}
