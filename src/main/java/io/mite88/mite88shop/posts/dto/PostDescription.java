package io.mite88.mite88shop.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

public record PostDescription(

        Long id,

        @Schema(
                description = "게시물 제목",
                example = "제목 1"
        )
        String title,

        @Schema(
                description = "게시물 네용",
                example = "내용 1"
        )
        String content,

        @Schema(
                description = "게시물 작성자",
                example = "작성자1"
        )
        String authorName,

        @Schema(
                description = "게시물 작성일",
                example = "2026-06-11T05:50:06.120Z"
        )
        LocalDateTime createdAt
) implements Serializable {
}