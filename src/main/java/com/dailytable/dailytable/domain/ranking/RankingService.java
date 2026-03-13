package com.dailytable.dailytable.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    public List<RecipeRankingDto> getTodayRanking(int limit) {
        log.info("Getting today ranking, limit: {}", limit);
        return rankingRepository.selectTodayBestRecipes(limit);
    }

    public List<RecipeRankingDto> getWeeklyRanking(int limit) {
        log.info("Getting weekly ranking, limit: {}", limit);
        return rankingRepository.selectWeeklyBestRecipes(limit);
    }
}
