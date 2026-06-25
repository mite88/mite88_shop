package io.mite88.mite88shop.global.exception;

import io.mite88.mite88shop.global.code.ResponseCode;
import lombok.Getter;

/**
 * 도메인 비즈니스 예외 - ResponseCode를 함께 전달하여 GlobalExceptionHandler에서 일관된 응답 생성
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResponseCode responseCode;

    /**
     * ResponseCode의 기본 메시지 사용
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    /**
     * 커스텀 메시지 사용
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
}
