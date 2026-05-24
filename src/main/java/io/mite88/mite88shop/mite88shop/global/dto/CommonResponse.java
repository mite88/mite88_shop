package io.mite88.mite88shop.mite88shop.global.dto;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공통 응답 객체")
public class CommonResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 코드", example = "S000")
    private String code;

    @Schema(description = "메시지", example = "성공")
    private String message;

    @Schema(description = "데이터")
    private T data;

    @Operation(summary = "성공 응답 생성", hidden = true)
    public static <T> CommonResponse<T> success(T data) {
        return of(ResponseCode.SUCCESS, data);
    }

    @Operation(summary = "성공 응답 생성", hidden = true)
    public static <T> CommonResponse<T> successWithMessage(T data, ResponseCode responseCode) {
        return CommonResponse.<T>builder()
                .success(true)
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .data(data)
                .build();
    }

    @Operation(summary = "공통 응답 생성", hidden = true)
    public static <T> CommonResponse<T> of(ResponseCode responseCode, T data) {
        return CommonResponse.<T>builder()
                .success(responseCode.getHttpStatus().is2xxSuccessful())
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .data(data)
                .build();
    }

    @Operation(summary = "실패 응답 생성", hidden = true)
    public static <T> CommonResponse<T> fail(ResponseCode responseCode) {
        return fail(responseCode, responseCode.getMessage());
    }

    @Operation(summary = "실패 응답 생성", hidden = true)
    public static <T> CommonResponse<T> fail(ResponseCode responseCode, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .code(responseCode.getCode())
                .message(message)
                .data(null)
                .build();
    }
}
