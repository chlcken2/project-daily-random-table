package com.dailytable.dailytable.domain.user;

import com.dailytable.dailytable.domain.auth.dto.entity.UserEntity;
import com.dailytable.dailytable.domain.user.dto.response.UserLikedRecipeResponse;
import com.dailytable.dailytable.domain.user.dto.response.UserMyRecipeResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    UserEntity selectById(@Param("id") Long id);

    int countMyRecipes(@Param("userId") Long userId);
    int countReceivedLikes(@Param("userId") Long userId);

    List<UserMyRecipeResponse> selectMyRecipes(@Param("userId") Long userId);
    List<UserLikedRecipeResponse> selectLikedRecipesByUserId(@Param("userId") Long userId);
}
