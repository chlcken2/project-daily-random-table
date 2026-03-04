package com.dailytable.dailytable.global.common;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류"),

    // Recipe
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다"),

    // Gacha
    GACHA_DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "오늘의 가챠 기회를 모두 사용했어요! 내일 다시 도전하세요"),
    GACHA_NO_INGREDIENTS(HttpStatus.BAD_REQUEST, "재료를 1개 이상 추가해주세요!"),
    GACHA_AI_FAILURE(HttpStatus.SERVICE_UNAVAILABLE, "현재 내부 서비스의 지연으로 통신이 원활하지 않습니다. 새로고침 후 이용 부탁드립니다~"),
    GACHA_SAVE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "레시피 저장에 실패했습니다. 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
