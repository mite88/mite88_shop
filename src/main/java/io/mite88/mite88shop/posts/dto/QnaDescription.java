package io.mite88.mite88shop.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

public record QnaDescription(

        Long id,

        @Schema(description = "문의 제목", example = "제품 관련 문의")
        String title,

        @Schema(description = "문의 내용", example = "문의 내용입니다.")
        String content,

        @Schema(description = "작성자", example = "admin")
        String authorName,

        @Schema(description = "작성일", example = "2026-06-28T12:00:00")
        LocalDateTime createdAt
) implements Serializable {}
