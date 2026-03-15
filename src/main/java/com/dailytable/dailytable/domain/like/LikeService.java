package com.dailytable.dailytable.domain.like;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;

    @Transactional
    public void like(Long recipeId, Long userId) {
        Long likeId = likeMapper.findLikeId(userId, recipeId);
        if (likeId == null) {
            likeMapper.insertLike(userId, recipeId);
            likeMapper.increaseLikeCount(recipeId);
        }
    }

    @Transactional
    public void unlike(Long recipeId, Long userId) {
        Long likeId = likeMapper.findLikeId(userId, recipeId);
        if (likeId != null) {
            likeMapper.deleteLike(userId, recipeId);
            likeMapper.decreaseLikeCount(recipeId);
        }
    }
}