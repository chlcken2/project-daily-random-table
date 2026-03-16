package com.dailytable.dailytable.domain.user;

import com.dailytable.dailytable.domain.auth.dto.entity.UserEntity;
import com.dailytable.dailytable.domain.gacha.GachaMapper;
import com.dailytable.dailytable.domain.user.dto.response.UserLikedRecipeResponse;
import com.dailytable.dailytable.domain.user.dto.response.UserMyRecipeResponse;
import com.dailytable.dailytable.domain.user.dto.response.UserProfileResponse;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserMapper userMapper;
    private final GachaMapper gachaMapper;

    public UserProfileResponse getProfile(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }
        int myRecipeCount = userMapper.countMyRecipes(userId);
        int receivedLikeCount = userMapper.countReceivedLikes(userId);
        int totalGachaCount = gachaMapper.countTotalGenerations(userId);

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .myRecipeCount(myRecipeCount)
                .receivedLikeCount(receivedLikeCount)
                .totalGachaCount(totalGachaCount)
                .build();
    }

    public List<UserMyRecipeResponse> getMyRecipes(Long userId) {
        return userMapper.selectMyRecipes(userId);
    }

    public List<UserLikedRecipeResponse> getLikedRecipes(Long userId) {
        return userMapper.selectLikedRecipesByUserId(userId);
    }
}
