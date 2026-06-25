package io.mite88.mite88shop.members.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record MemberSaveRequest(

        @NotBlank
        @Length(min = 4, max = 10)
        String username,

        @NotBlank
        @Length(min = 8, max = 12)
        String password,

        @NotBlank
        String email
) {
}
