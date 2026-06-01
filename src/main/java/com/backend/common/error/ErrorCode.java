package com.backend.common.error;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

	/**
	 * Common Error
	 */
	BAD_REQUEST(ErrorDomain.COMMON, "BAD_REQUEST", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	NOT_FOUND(ErrorDomain.COMMON, "NOT_FOUND", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
	INVALID_INPUT(ErrorDomain.COMMON, "INVALID_INPUT", HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
	INTERNAL_SERVER_ERROR(
		ErrorDomain.COMMON,
		"INTERNAL_SERVER_ERROR",
		HttpStatus.INTERNAL_SERVER_ERROR,
		"서버 오류가 발생했습니다."
	),
	UNAUTHORIZED_EXCEPTION(ErrorDomain.COMMON, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "승인되지 않은 요청입니다."),
	ACCESS_DENIED(ErrorDomain.COMMON, "ACCESS_DENIED", HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

	/**
	 * Database Error
	 */
	DATABASE_ERROR(ErrorDomain.DATABASE, "ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류입니다."),
	DUPLICATED_KEY(ErrorDomain.DATABASE, "DUPLICATED_KEY", HttpStatus.CONFLICT, "중복된 키입니다."),

	/**
	 * User Error
	 */
	USER_NOT_FOUND(ErrorDomain.USER, "NOT_FOUND", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
	INVALID_USER(ErrorDomain.USER, "INVALID", HttpStatus.BAD_REQUEST, "유효하지 않은 사용자입니다."),
	NICKNAME_DUPLICATED(ErrorDomain.USER, "NICKNAME_DUPLICATED", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
	ROLE_CHANGE_NOT_ALLOWED(ErrorDomain.USER, "ROLE_CHANGE_NOT_ALLOWED", HttpStatus.FORBIDDEN, "역할을 변경할 수 없는 상태입니다."),
	INVALID_ROLE(ErrorDomain.USER, "INVALID_ROLE", HttpStatus.BAD_REQUEST, "유효하지 않은 역할입니다."),

	/**
	 * Auth/JWT Error
	 */
	MALFORMED_TOKEN_EXCEPTION(ErrorDomain.AUTH, "MALFORMED_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 형식입니다."),
	INVALID_TOKEN_TYPE(ErrorDomain.AUTH, "INVALID_TOKEN_TYPE", HttpStatus.UNAUTHORIZED, "잘못된 토큰 타입입니다."),
	TOKEN_EXPIRED_EXCEPTION(ErrorDomain.AUTH, "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
	TOKEN_BLACKLISTED_EXCEPTION(ErrorDomain.AUTH, "TOKEN_BLACKLISTED", HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
	TOKEN_NOT_FOUND(ErrorDomain.AUTH, "TOKEN_NOT_FOUND", HttpStatus.NOT_FOUND, "요청으로부터 토큰을 찾지 못했습니다."),
	REFRESH_TOKEN_NOT_MATCH(ErrorDomain.AUTH, "REFRESH_TOKEN_NOT_MATCH", HttpStatus.BAD_REQUEST, "리프레시 토큰이 일치하지 않습니다."),
	INVALID_TOKEN(ErrorDomain.AUTH, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	INVALID_TOKEN_SIGNATURE(ErrorDomain.AUTH, "INVALID_TOKEN_SIGNATURE", HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다."),
	TOKEN_PROCESSING_ERROR(ErrorDomain.AUTH, "TOKEN_PROCESSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "토큰 처리 중 오류가 발생했습니다."),

	/**
	 * Menu Error
	 */
	MENU_NOT_FOUND(ErrorDomain.MENU, "NOT_FOUND", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
	MENU_BEAN_ALREADY_MAPPED(ErrorDomain.MENU, "BEAN_ALREADY_MAPPED", HttpStatus.CONFLICT, "해당 원두는 이미 메뉴에 연결되어 있습니다."),
	MENU_ACCESS_DENIED(ErrorDomain.MENU, "ACCESS_DENIED", HttpStatus.FORBIDDEN, "메뉴에 대한 접근 권한이 없습니다."),
	MENU_ALREADY_DELETED(ErrorDomain.MENU, "ALREADY_DELETED", HttpStatus.CONFLICT, "이미 삭제된 메뉴입니다."),

	/**
	 * Store Error
	 */
	STORE_NOT_FOUND(ErrorDomain.STORE, "NOT_FOUND", HttpStatus.NOT_FOUND, "가맹점을 찾을 수 없습니다."),
	INVALID_LOCATION(ErrorDomain.STORE, "INVALID_LOCATION", HttpStatus.BAD_REQUEST, "유효하지 않은 위치 정보입니다."),
	STORE_ALREADY_EXISTS(ErrorDomain.STORE, "ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 등록된 가맹점입니다."),
	STORE_OWNER_ONLY(ErrorDomain.STORE, "OWNER_ONLY", HttpStatus.FORBIDDEN, "본인 소유의 가맹점만 수정할 수 있습니다."),

	/**
	 * Owner Application Error
	 */
	OWNER_APPLICATION_NOT_FOUND(
		ErrorDomain.OWNER_APPLICATION,
		"NOT_FOUND",
		HttpStatus.NOT_FOUND,
		"가맹점주 신청을 찾을 수 없습니다."
	),
	OWNER_APPLICATION_ALREADY_PENDING(
		ErrorDomain.OWNER_APPLICATION,
		"ALREADY_PENDING",
		HttpStatus.CONFLICT,
		"이미 처리 대기 중인 가맹점주 신청이 있습니다."
	),
	OWNER_APPLICATION_NOT_PENDING(
		ErrorDomain.OWNER_APPLICATION,
		"NOT_PENDING",
		HttpStatus.CONFLICT,
		"처리 대기 중인 신청만 변경할 수 있습니다."
	),
	OWNER_APPLICATION_NOT_ALLOWED(
		ErrorDomain.OWNER_APPLICATION,
		"NOT_ALLOWED",
		HttpStatus.FORBIDDEN,
		"일반 사용자만 가맹점주 신청을 할 수 있습니다."
	),

	/**
	 * Visit Error
	 */
	VISIT_NOT_FOUND(ErrorDomain.VISIT, "NOT_FOUND", HttpStatus.NOT_FOUND, "방문 기록을 찾을 수 없습니다."),
	VISIT_NOT_BELONG_TO_USER(ErrorDomain.VISIT, "NOT_BELONG_TO_USER", HttpStatus.FORBIDDEN, "해당 방문 기록에 대한 권한이 없습니다."),

	/**
	 * Review Error
	 */
	REVIEW_NOT_FOUND(ErrorDomain.REVIEW, "NOT_FOUND", HttpStatus.NOT_FOUND, "리뷰 기록을 찾을 수 없습니다."),
	ALREADY_DELETED_REVIEW(ErrorDomain.REVIEW, "ALREADY_DELETED", HttpStatus.BAD_REQUEST, "이미 삭제 처리된 리뷰입니다."),
	REVIEW_ALREADY_EXISTS_FOR_VISIT(ErrorDomain.REVIEW, "ALREADY_EXISTS_FOR_VISIT", HttpStatus.CONFLICT, "해당 방문에 대한 리뷰가 이미 존재합니다."),
	CUPPING_NOTE_NOT_FOUND(ErrorDomain.CUPPING_NOTE, "NOT_FOUND", HttpStatus.NOT_FOUND, "커핑 노트를 찾을 수 없습니다."),
	CUPPING_NOTE_ALREADY_EXISTS(ErrorDomain.CUPPING_NOTE, "ALREADY_EXISTS", HttpStatus.CONFLICT, "해당 리뷰에 대한 커핑 노트가 이미 존재합니다."),

	/**
	 * Roastery Error
	 */
	ROASTERY_NOT_FOUND(ErrorDomain.ROASTERY, "NOT_FOUND", HttpStatus.NOT_FOUND, "로스터리를 찾을 수 없습니다."),

	/**
	 * Bean Error
	 */
	BEAN_NOT_FOUND(ErrorDomain.BEAN, "NOT_FOUND", HttpStatus.NOT_FOUND, "원두를 찾을 수 없습니다."),
	BEAN_ACCESS_DENIED(ErrorDomain.BEAN, "ACCESS_DENIED", HttpStatus.FORBIDDEN, "원두에 대한 접근 권한이 없습니다."),

	/**
	 * File Error
	 */
	INVALID_FILE_NAME(ErrorDomain.FILE, "INVALID_NAME", HttpStatus.BAD_REQUEST, "파일 이름이 유효하지 않습니다."),
	INVALID_FILE_EXTENSION(ErrorDomain.FILE, "INVALID_EXTENSION", HttpStatus.BAD_REQUEST, "파일 타입이 유효하지 않습니다."),
	IMAGE_UPLOAD_FAILED(ErrorDomain.FILE, "IMAGE_UPLOAD_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),

	/**
	 * Passport Error
	 */
	PASSPORT_NOT_FOUND(ErrorDomain.PASSPORT, "NOT_FOUND", HttpStatus.NOT_FOUND, "여권을 찾을 수 없습니다."),
	INVALID_YEAR(ErrorDomain.PASSPORT, "INVALID_YEAR", HttpStatus.BAD_REQUEST, "유효하지 않은 연도입니다."),
	PASSPORT_NOT_AVAILABLE_YET(ErrorDomain.PASSPORT, "NOT_AVAILABLE_YET", HttpStatus.BAD_REQUEST, "아직 조회할 수 없는 여권입니다."),
	PASSPORT_GENERATION_FAILED(ErrorDomain.PASSPORT, "GENERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "여권 생성에 실패했습니다."),
	PASSPORT_ACCESS_DENIED(ErrorDomain.PASSPORT, "ACCESS_DENIED", HttpStatus.FORBIDDEN, "여권 조회 권한이 없습니다."),

	/**
	 * Preference Error
	 */
	PREFERENCE_NOT_FOUND(ErrorDomain.PREFERENCE, "NOT_FOUND", HttpStatus.NOT_FOUND, "사용자 취향 정보를 찾을 수 없습니다."),
	PREFERENCE_ALREADY_EXISTS(ErrorDomain.PREFERENCE, "ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 취향 정보가 존재합니다."),

	/**
	 * BeanScore Error
	 */
	BEAN_SCORE_NOT_FOUND(ErrorDomain.BEAN_SCORE, "NOT_FOUND", HttpStatus.NOT_FOUND, "원두 점수를 찾을 수 없습니다."),
	BEAN_SCORE_ALREADY_EXISTS(ErrorDomain.BEAN_SCORE, "ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 원두 점수가 존재합니다."),

	/**
	 * AI Error
	 */
	AI_IMAGE_GENERATION_FAILED(ErrorDomain.AI, "IMAGE_GENERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "AI 이미지 생성에 실패했습니다."),
	AI_IMAGE_EMPTY_RESPONSE(ErrorDomain.AI, "IMAGE_EMPTY_RESPONSE", HttpStatus.INTERNAL_SERVER_ERROR, "AI 이미지 생성 응답이 비어있습니다."),

	/**
	 * Batch Error
	 */
	BATCH_NOT_FOUND(ErrorDomain.BATCH, "NOT_FOUND", HttpStatus.NOT_FOUND, "배치 작업을 찾을 수 없습니다."),

	/**
	 * Bookmark Error
	 */
	BOOKMARK_FOLDER_NOT_FOUND(ErrorDomain.BOOKMARK, "FOLDER_NOT_FOUND", HttpStatus.NOT_FOUND, "북마크 폴더를 찾을 수 없습니다."),
	BOOKMARK_FOLDER_ACCESS_DENIED(ErrorDomain.BOOKMARK, "FOLDER_ACCESS_DENIED", HttpStatus.FORBIDDEN, "북마크 폴더에 대한 접근 권한이 없습니다."),
	BOOKMARK_ITEM_NOT_FOUND(ErrorDomain.BOOKMARK, "ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, "북마크 항목을 찾을 수 없습니다."),
	BOOKMARK_ITEM_ALREADY_EXISTS(ErrorDomain.BOOKMARK, "ITEM_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 폴더에 저장된 카페입니다."),
	;

	private final ErrorDomain domain;
	private final String localCode;
	private final HttpStatus httpStatus;
	private final String message;

	public String getCode() {
		return domain.name() + "." + localCode;
	}

	private enum ErrorDomain {
		COMMON,
		DATABASE,
		USER,
		AUTH,
		MENU,
		STORE,
		OWNER_APPLICATION,
		VISIT,
		REVIEW,
		CUPPING_NOTE,
		ROASTERY,
		BEAN,
		FILE,
		PASSPORT,
		PREFERENCE,
		BEAN_SCORE,
		AI,
		BATCH,
		BOOKMARK
	}
}
