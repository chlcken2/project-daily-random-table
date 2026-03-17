package com.dailytable.dailytable.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingMapper rankingMapper;

    public List<RankingDto> getTodayRanking(int limit) {
        log.info("Getting today ranking, limit: {}", limit);
        return rankingMapper.selectTodayBestRecipes(limit);
    }

    public List<RankingDto> getWeeklyRanking(int limit) {
        log.info("Getting weekly ranking, limit: {}", limit);
        return rankingMapper.selectWeeklyBestRecipes(limit);
    }
}
