package com.dailytable.dailytable.domain.ranking;

import com.dailytable.dailytable.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 랭킹 시스템 - 오늘의 식단 & 명예의 식단
 * 실시간 인기도 점수 계산: (좋아요×3) + (댓글×2) + (조회수×1)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    /**
     * [오늘의 식단] 당일 생성된 레시피 중 인기도 Top 5
     * 조회 시점: created_at >= CURDATE() (오늘 00:00 이후)
     * 정렬: popularity_score DESC (인기도 높은 순)
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<RankingDto>>> getTodayRanking(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/ranking/today, limit: {}", limit);
        List<RankingDto> ranking = rankingService.getTodayRanking(limit);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }

    /**
     * [명예의 식단] 최근 7일간 인기 레시피 Top 5
     * 조회 시점: created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
     * 정렬: popularity_score DESC (인기도 높은 순)
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<RankingDto>>> getWeeklyRanking(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/ranking/weekly, limit: {}", limit);
        List<RankingDto> ranking = rankingService.getWeeklyRanking(limit);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }
}
