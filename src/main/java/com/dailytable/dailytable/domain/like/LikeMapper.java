package com.dailytable.dailytable.domain.like;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

	Long findLikeId(@Param("userId") Long userId,
            @Param("recipeId") Long recipeId);
	
    void insertLike(@Param("userId") Long userId,
                    @Param("recipeId") Long recipeId);

    void deleteLike(@Param("userId") Long userId,
                    @Param("recipeId") Long recipeId);

    void increaseLikeCount(@Param("recipeId") Long recipeId);

    void decreaseLikeCount(@Param("recipeId") Long recipeId);
}