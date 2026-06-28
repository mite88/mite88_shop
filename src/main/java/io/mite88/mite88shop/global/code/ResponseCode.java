package io.mite88.mite88shop.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API 응답 코드 정의 - HTTP 상태 + 코드 문자열 + 한국어 메시지로 구성
 * BusinessException 발생 시 GlobalExceptionHandler가 이 값을 기반으로 응답 직렬화
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // Success
    SUCCESS(HttpStatus.OK, "S000", "성공"),

    // Member
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M001", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "M003", "이미 존재하는 회원 정보입니다."),

    // Auth
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A001", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A002", "존재하지 않는 사용자입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 RefreshToken입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A004", "만료되었거나 로그아웃된 RefreshToken입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A005", "제공된 RefreshToken이 현재 유효한 RefreshToken과 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "A006", "인증되지 않은 접근입니다."),

    // Common
    INPUT_REQUIRED(HttpStatus.BAD_REQUEST, "C001", "입력값이 필요합니다."),

    // AI Job
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "J001", "작업을 찾을 수 없습니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_POST_UPDATE(HttpStatus.FORBIDDEN, "P001", "본인 글이 아니면 수정할 수 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_COMMENT_DELETE(HttpStatus.FORBIDDEN, "C003", "본인 댓글만 삭제할 수 있습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PR002", "재고가 부족합니다."),

    // Cart
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "장바구니 항목을 찾을 수 없습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OR001", "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "OR002", "취소할 수 없는 주문 상태입니다."),
    EMPTY_CART(HttpStatus.BAD_REQUEST, "OR003", "장바구니가 비어있습니다."),

    // Server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "서버 내부 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}