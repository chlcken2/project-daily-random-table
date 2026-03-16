package com.dailytable.dailytable.domain.like;

import lombok.Data;
//LikeDto
@Data
public class LikeDto {
	private Long recipeId;
	private boolean isLiked;
}
