package com.backend.common.error;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorCode {

	/**
	 * Common Error
	 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "C-001", "잘못된 요청입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "C-002", "리소스를 찾을 수 없습니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "C-003", "유효하지 않은 입력값입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-004", "서버 오류가 발생했습니다."),
	UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED, "C-005", "승인되지 않은 요청입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "C-006", "접근이 거부되었습니다."),

	/**
	 * Database Error
	 */
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D-001", "데이터베이스 오류입니다."),
	DUPLICATED_KEY(HttpStatus.CONFLICT, "D-002", "중복된 키입니다."),
	/**
	 * User Error
	 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-001", "유저를 찾을 수 없습니다."),
	NICKNAME_NOT_BLANK(HttpStatus.BAD_REQUEST, "U-002", "닉네임은 필수 입력값입니다."),
	NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "U-003", "닉네임 형식이 올바르지 않습니다."),
	INVALID_USER(HttpStatus.BAD_REQUEST, "U-004", "유효하지 않은 사용자입니다."),
	NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "U-005", "이미 사용 중인 닉네임입니다."),
	ROLE_CHANGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "U-006", "역할을 변경할 수 없는 상태입니다."),
	INVALID_ROLE(HttpStatus.BAD_REQUEST, "U-007", "유효하지 않은 역할입니다."),
	/**
	 * Auth/JWT Error
	 */
	MALFORMED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "A-001", "유효하지 않은 토큰 형식입니다."),
	INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "A-002", "잘못된 토큰 타입입니다."),
	TOKEN_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, "A-003", "만료된 토큰입니다."),
	TOKEN_BLACKLISTED_EXCEPTION(HttpStatus.UNAUTHORIZED, "A-004", "블랙리스트에 등록된 토큰입니다."),
	TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "A-005", "요청으로부터 토큰을 찾지 못했습니다."),
	REFRESH_TOKEN_NOT_MATCH(HttpStatus.BAD_REQUEST, "A-006", "리프레시 토큰이 일치하지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A-007", "유효하지 않은 토큰입니다."),
	INVALID_TOKEN_SIGNATURE(HttpStatus.UNAUTHORIZED, "A-008", "토큰 서명이 유효하지 않습니다."),
	TOKEN_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A-009", "토큰 처리 중 오류가 발생했습니다."),

	/**
	 * Menu Error
	 */
	MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M-001", "메뉴를 찾을 수 없습니다."),
	MENU_ID_REQUIRED(HttpStatus.BAD_REQUEST, "M-002", "메뉴 ID가 필요합니다."),
	MENU_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "M-003", "메뉴 이름은 필수 입력값입니다."),
	MENU_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "M-004", "메뉴 가격은 필수 입력값입니다."),
	MENU_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "M-005", "메뉴 카테고리는 필수 입력값입니다."),
	MENU_BEAN_ALREADY_MAPPED(HttpStatus.CONFLICT, "M-011", "해당 원두는 이미 메뉴에 연결되어 있습니다."),
	MENU_ACCESS_DENIED(HttpStatus.FORBIDDEN, "M-012", "메뉴에 대한 접근 권한이 없습니다."),
	MENU_ALREADY_DELETED(HttpStatus.CONFLICT, "M-013", "이미 삭제된 메뉴입니다."),

	/**
	 * Store Error
	 */
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S-001", "가맹점을 찾을 수 없습니다."),
	STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S-002", "가맹점 접근 권한이 없습니다."),
	INVALID_LOCATION(HttpStatus.BAD_REQUEST, "S-003", "유효하지 않은 위치 정보입니다."),
	STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "S-004", "이미 등록된 가맹점입니다."),
	STORE_OWNER_ONLY(HttpStatus.FORBIDDEN, "S-005", "본인 소유의 가맹점만 수정할 수 있습니다."),

	/**
	 * Visit Error
	 */
	INVALID_VISIT_REQUEST(HttpStatus.BAD_REQUEST, "V-002", "방문 기록 요청 데이터가 유효하지 않습니다."),
	LOCATION_REQUIRED(HttpStatus.BAD_REQUEST, "V-003", "위치 정보가 필요합니다."),
	COORDINATES_REQUIRED(HttpStatus.BAD_REQUEST, "V-004", "위도와 경도는 필수입니다."),
	LOCATION_OUT_OF_KOREA(HttpStatus.BAD_REQUEST, "V-005", "위치가 한국 내부가 아닙니다."),
	VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "V-006", "방문 기록을 찾을 수 없습니다."),
	VISIT_NOT_BELONG_TO_USER(HttpStatus.FORBIDDEN, "V-007", "해당 방문 기록에 대한 권한이 없습니다."),

	/**
	 * Review Error
	 */
	INVALID_REVIEW_REQUEST(HttpStatus.BAD_REQUEST, "R-001", "리뷰 요청 데이터가 유효하지 않습니다."),
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R-002", "리뷰 기록을 찾을 수 없습니다."),
	ALREADY_DELETED_REVIEW(HttpStatus.BAD_REQUEST, "R-005", "이미 삭제 처리된 리뷰입니다."),
	REVIEW_ALREADY_EXISTS_FOR_VISIT(HttpStatus.CONFLICT, "R-006", "해당 방문에 대한 리뷰가 이미 존재합니다."),
	CUPPING_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "R-007", "커핑 노트를 찾을 수 없습니다."),
	CUPPING_NOTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "R-008", "해당 리뷰에 대한 커핑 노트가 이미 존재합니다."),

	/**
	 * Roastery Error
	 */
	ROASTERY_NOT_FOUND(HttpStatus.NOT_FOUND, "RO-001", "로스터리를 찾을 수 없습니다."),
	ROASTERY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "RO-002", "로스터리 이름은 필수 입력값입니다."),
	INVALID_ROASTERY_REQUEST(HttpStatus.BAD_REQUEST, "RO-003", "로스터리 요청 데이터가 유효하지 않습니다."),
	ROASTERY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RO-007", "로스터리에 대한 접근 권한이 없습니다."),

	/**
	 * Bean Error
	 */
	BEAN_NOT_FOUND(HttpStatus.NOT_FOUND, "B-001", "원두를 찾을 수 없습니다."),
	BEAN_COUNTRY_REQUIRED(HttpStatus.BAD_REQUEST, "B-002", "원두 생산 국가는 필수 입력값입니다."),
	INVALID_BEAN_REQUEST(HttpStatus.BAD_REQUEST, "B-003", "원두 요청 데이터가 유효하지 않습니다."),
	BEAN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B-007", "원두에 대한 접근 권한이 없습니다."),

	/**
	 * File Error
	 */
	INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "F-001", "파일 이름이 유효하지 않습니다."),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "F-002", "파일 타입이 유효하지 않습니다."),
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F-003", "이미지 업로드에 실패했습니다."),

	/**
	 * Passport Error
	 */
	PASSPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "P-001", "여권을 찾을 수 없습니다."),
	INVALID_YEAR(HttpStatus.BAD_REQUEST, "P-002", "유효하지 않은 연도입니다."),
	PASSPORT_NOT_AVAILABLE_YET(HttpStatus.BAD_REQUEST, "P-003", "아직 조회할 수 없는 여권입니다."),
	PASSPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P-004", "여권 생성에 실패했습니다."),
	AI_IMAGE_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "P-005", "AI 이미지 생성 서비스 연결 실패"),
	PASSPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "P-006", "여권이 이미 존재합니다."),
	PASSPORT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P-007", "여권 조회 권한이 없습니다."),
	NO_VISIT_RECORDS(HttpStatus.NOT_FOUND, "P-008", "해당 기간에 방문 기록이 없습니다."),

	/**
	 * Preference Error
	 */
	PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PR-001", "사용자 취향 정보를 찾을 수 없습니다."),
	PREFERENCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "PR-002", "이미 취향 정보가 존재합니다."),

	/**
	 * BeanScore Error
	 */
	BEAN_SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "BS-001", "원두 점수를 찾을 수 없습니다."),
	BEAN_SCORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "BS-002", "이미 원두 점수가 존재합니다."),

	/**
	 * Recommendation Error
	 */
	RECOMMENDATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RC-001", "추천 생성에 실패했습니다."),
	EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RC-002", "임베딩 생성에 실패했습니다."),
	LLM_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "RC-003", "LLM 서비스 연결에 실패했습니다."),
	INSUFFICIENT_DATA(HttpStatus.BAD_REQUEST, "RC-004", "추천을 위한 데이터가 부족합니다."),

	/**
	 * AI Error
	 */
	AI_IMAGE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI-001", "AI 이미지 생성에 실패했습니다."),
	AI_IMAGE_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "AI-002", "AI 이미지 생성 응답이 비어있습니다."),
	AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI-003", "AI 서비스를 사용할 수 없습니다."),
	USER_PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "AI-004", "사용자 프롬프트는 필수 입력값입니다."),
	AI_IMAGE_DATA_EMPTY(HttpStatus.BAD_REQUEST, "AI-005", "이미지 데이터는 비어있을 수 없습니다."),
	AI_MIME_TYPE_EMPTY(HttpStatus.BAD_REQUEST, "AI-006", "MIME 타입은 비어있을 수 없습니다."),

	/**
	 * Batch Error
	 */
	BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "BA-001", "배치 작업을 찾을 수 없습니다."),
	BATCH_ALREADY_COMPLETED(HttpStatus.CONFLICT, "BA-002", "이미 완료된 배치 작업입니다."),

	/**
	 * Bookmark Error
	 */
	BOOKMARK_FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "BK-001", "북마크 폴더를 찾을 수 없습니다."),
	BOOKMARK_FOLDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BK-002", "북마크 폴더에 대한 접근 권한이 없습니다."),
	BOOKMARK_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "BK-003", "북마크 항목을 찾을 수 없습니다."),
	BOOKMARK_ITEM_ALREADY_EXISTS(HttpStatus.CONFLICT, "BK-004", "이미 폴더에 저장된 카페입니다."),
	BOOKMARK_FOLDER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "BK-005", "폴더 이름은 필수 입력값입니다."),
	BOOKMARK_INVALID_ICON(HttpStatus.BAD_REQUEST, "BK-006", "유효하지 않은 아이콘입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
