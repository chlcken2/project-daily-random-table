package com.dailytable.dailytable.domain.ranking;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ============================================
 * 레시피 랭킹/목록용 DTO (RecipeRankingDto.java)
 * ============================================
 * 
 * [파일 역할]
 * - 홈페이지 랭킹 표시와 레시피 목록에 사용되는 데이터 전송 객체
 * - DB에서 조회한 레시피 정보를 프론트엔드에 전달하는 그릇(容器)
 * 
 * [사용되는 화면]
 * 1. 홈페이지 "오늘의 식단" (당일 인기글) 랭킹
 * 2. 홈페이지 "명예의 식단" (주간 인기글) 랭킹  
 * 3. 홈페이지 "모두의 식탁" (공개 레시피) 그리드
 * 4. 마이페이지 내 레시피 목록
 * 
 * [데이터 흐름]
 * DB → MyBatis(Mapper) → RecipeRankingDto → JSON 변환 → JavaScript → HTML 렌더링
 * 
 * [주요 필드 설명]
 * - likeCount, commentCount, viewCount: 인기도 계산에 사용
 * - popularityScore: (좋아요×3) + (댓글×2) + (조회수×1) 계산된 값
 * - difficultyLabel: 난이도명 (예: "低", "中", "高")
 * - createdAt: 생성일시 (LocalDateTime → JavaScript에서 YYYY/MM/DD로 포맷팅)
 * 
 * [@Data 어노테이션 (Lombok)]
 * - Getter/Setter 자동 생성
 * - toString(), equals(), hashCode() 자동 생성
 * - 코드 간결화 및 유지보수성 향상
 */
@Data
@Builder
public class RecipeRankingDto {
    private Long id;                    // 레시피 고유 ID (PK)
    private String title;               // 레시피 제목
    private String titleImage;          // 대표 이미지 URL
    private String nickname;            // 작성자 닉네임 (JOIN으로 조회)
    private int likeCount;              // 좋아요 수 (인기도 계산용)
    private int commentCount;           // 댓글 수 (인기도 계산용)
    private int viewCount;              // 조회수 (인기도 계산용)
    private double popularityScore;     // 인기도 점수 (좋아요×3 + 댓글×2 + 조회수×1)
    private String difficultyLabel;     // 난이도명 (예: "低", "中", "高")
    private int cookingTime;            // 조리 시간 (분)
    private LocalDateTime createdAt;    // 생성 일시 (Java 8 DateTime API)
}
