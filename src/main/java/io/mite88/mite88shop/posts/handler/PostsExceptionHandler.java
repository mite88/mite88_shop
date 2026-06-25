package io.mite88.mite88shop.posts.handler;

import io.mite88.mite88shop.posts.exceptions.UnAuthorizedUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class PostsExceptionHandler {

    /**
     * 게시글 수정 권한 없음 - 403 응답
     */
    @ExceptionHandler(UnAuthorizedUpdateException.class)
    public ResponseEntity<String> handleUnAuthorizedUpdateException(
            UnAuthorizedUpdateException exception
    ) {
        return ResponseEntity.status(
                HttpStatus.FORBIDDEN
        ).body(exception.getMessage());
    }

}
