package io.mite88.mite88shop.global.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * packageName    : io.mite88.mite88shop.mite88shop.global.config.model
 * fileName       : AiJob
 * author         : Admin
 * date           : 26. 6. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 12.        Admin       최초 생성
 */
@Builder
public record AiJob(
        String jobId,
        String status,       // PENDING, PROCESSING, DONE, FAILED
        String input,
        Object result,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}