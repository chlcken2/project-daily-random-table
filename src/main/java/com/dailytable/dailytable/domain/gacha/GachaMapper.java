package com.dailytable.dailytable.domain.gacha;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GachaMapper {

    /** 당일 가챠 가능 횟수(당일 돌린 횟수) */
    int countTodayGenerations(@Param("userId") Long userId);

    /** 가챠 총 횟수 (전체 누적) */
    int countTotalGenerations(@Param("userId") Long userId);
}
