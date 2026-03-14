package com.dailytable.dailytable.domain.user;

import com.dailytable.dailytable.domain.auth.dto.entity.UserEntity;
import com.dailytable.dailytable.domain.gacha.GachaMapper;
import com.dailytable.dailytable.domain.user.dto.response.UserProfileResponse;
import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        int gachaCount = gachaMapper.countTodayGenerations(userId);

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .myRecipeCount(myRecipeCount)
                .receivedLikeCount(receivedLikeCount)
                .gachaCount(gachaCount)
                .build();
    }
}
