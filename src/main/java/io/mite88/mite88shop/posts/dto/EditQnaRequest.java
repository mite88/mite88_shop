package io.mite88.mite88shop.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record EditQnaRequest(

        @Schema(
                description = "문의 제목",
                example = "제품 관련 문의",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 10,
                maxLength = 100
        )
        @NotBlank
        @Length(min = 10, max = 100)
        String title,

        @Schema(
                description = "문의 내용",
                example = "내용을 입력해주세요.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1,
                maxLength = 250
        )
        @NotBlank
        @Length(min = 1, max = 250)
        String content
) {}
