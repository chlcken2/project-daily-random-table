package com.dailytable.dailytable.domain.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    boolean existsByEmail(@Param("email") String email);
    boolean existsByNickname(@Param("nickname") String nickname);
    UserEntity selectByEmail(@Param("email") String email);
    void insert(UserEntity user);
}
