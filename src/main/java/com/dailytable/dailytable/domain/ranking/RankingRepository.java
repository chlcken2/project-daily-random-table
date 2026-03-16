package com.dailytable.dailytable.domain.ranking;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingRepository {

    List<RecipeRankingDto> selectTodayBestRecipes(@Param("limit") int limit);

    List<RecipeRankingDto> selectWeeklyBestRecipes(@Param("limit") int limit);
}
