package com.dailytable.dailytable.domain.user;

import com.dailytable.dailytable.domain.auth.dto.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserEntity selectById(@Param("id") Long id);
}
