package com.dailytable.dailytable.domain.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface RefreshTokenMapper {

    void insert(RefreshToken rt);
    void deleteByToken(@Param("token") String token);
    RefreshToken selectByTokenAndNotExpired(@Param("token") String token, @Param("now") LocalDateTime now);
}
