package io.mite88.mite88shop.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record EditPostRequest(

        @Schema(
                description = "작성하고자 하는 게시물 제목 입력",
                example = "제목 1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 10,
                maxLength = 100
        )
        @NotBlank
        @Length(min = 10, max = 100)
        String title,

        @Schema(
                description = "작성하고자 하는 게시물 내용 입력",
                example = "내용 1",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1,
                maxLength = 250
        )
        @NotBlank
        @Length(min = 1, max = 250)
        String content
) {
}
