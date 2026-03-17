package com.dailytable.dailytable.global.common;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * ============================================
 * 에러 코드 정의 (ErrorCode.java)
 * ============================================
 *
 * [파일 역할]
 * - 애플리케이션 전역에서 사용하는 에러 코드 enum
 * - HTTP 상태 코드와 사용자 메시지를 함께 관리
 * - 한국어로 된 에러 메시지를 제공 (사용자 표시용)
 *
 * [사용 예시]
 * throw new BaseException(ErrorCode.RECIPE_NOT_FOUND);
 * → GlobalExceptionHandler에서 catch → JSON 에러 응답 생성
 *
 * [에러 응답 형식]
 * {
 *   "success": false,
 *   "message": "レシピが見つかりません",
 *   "errorCode": "RECIPE_NOT_FOUND"
 * }
 *
 * [에러 코드 분류]
 * - Common: 공통 에러 (사용자 없음, 잘못된 요청, 서버 에러)
 * - Recipe: 레시피 관련 (레시피 없음)
 * - Gacha: 가챠 관련 (일일 한도, 재료 없음, AI 실패 등)
 * - Auth: 인증 관련 (회원가입, 로그인 에러)
 * - User: 사용자 관련 (권한 없음)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "不正なリクエストです"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "サーバーエラーです"),

    // Recipe
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "レシピが見つかりません"),

    // Gacha
    GACHA_DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "本日のガチャ回数はすべて使用しました！明日また挑戦してください"),
    GACHA_NO_INGREDIENTS(HttpStatus.BAD_REQUEST, "材料を1つ以上追加してください！"),
    GACHA_AI_FAILURE(HttpStatus.SERVICE_UNAVAILABLE, "現在、内部サービスの遅延により通信が不安定です。ページを再読み込みしてご利用ください"),
    GACHA_SAVE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "レシピの保存に失敗しました。もう一度お試しください"),

    // Auth - 회원가입
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "すでに登録されているメールアドレスです"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "すでに使用されているニックネームです"),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "ニックネームを入力してください"),
    CONFIRM_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "パスワード確認を入力してください"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "パスワードが一致しません"),
    // Auth - 로그인 (상황별 분기)
    EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "メールアドレスを入力してください"),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "パスワードを入力してください"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "パスワードが一致しません"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードをご確認ください"),

    // User -  프로필조회
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ログインが必要です");

    private final HttpStatus status;
    private final String message;
}