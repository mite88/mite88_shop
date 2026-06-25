package io.mite88.mite88shop.global.config;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.dto.CommonResponse;
import io.mite88.mite88shop.global.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 비즈니스 예외 처리 - ResponseCode에 정의된 HTTP 상태와 메시지로 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        ResponseCode responseCode = e.getResponseCode();
        return ResponseEntity
                .status(responseCode.getHttpStatus())
                .body(CommonResponse.fail(responseCode, e.getMessage()));
    }

    /**
     * 사용자 미존재 예외 처리
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity
                .status(ResponseCode.USER_NOT_FOUND.getHttpStatus())
                .body(CommonResponse.fail(ResponseCode.USER_NOT_FOUND));
    }

    /**
     * DB unique 제약 위반 처리 (중복 회원 등)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(ResponseCode.DUPLICATE_MEMBER.getHttpStatus())
                .body(CommonResponse.fail(ResponseCode.DUPLICATE_MEMBER));
    }

    /**
     * 처리되지 않은 모든 예외 처리 - 500 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleGeneralException(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(CommonResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR));
    }
}