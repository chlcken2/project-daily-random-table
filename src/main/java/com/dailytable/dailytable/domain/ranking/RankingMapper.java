package com.dailytable.dailytable.domain.ranking;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    List<RankingDto> selectTodayBestRecipes(@Param("limit") int limit);

    List<RankingDto> selectWeeklyBestRecipes(@Param("limit") int limit);
}
