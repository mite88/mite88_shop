package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.dto.QnaPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

public interface QnaApiDocs {

    @Operation(summary = "문의 작성", description = "새 문의를 작성합니다. (ADMIN 전용)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "작성된 문의",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = QnaDescription.class)))
    ResponseEntity<QnaDescription> saveQna(@RequestBody EditQnaRequest request, Principal principal);

    @Operation(summary = "문의 단건 조회", description = "문의 ID로 단건 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회된 문의",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = QnaDescription.class)))
    @ApiResponse(responseCode = "404", description = "문의 없음", content = @Content)
    ResponseEntity<QnaDescription> findById(@PathVariable Long id);

    @Operation(summary = "문의 목록 페이징 조회", description = "page 파라미터(0-indexed)로 10개씩 최신순 조회합니다.")
    @ApiResponse(responseCode = "200", description = "페이징된 문의 목록",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = QnaPageResponse.class)))
    ResponseEntity<QnaPageResponse> findAll(@RequestParam(defaultValue = "0") int page);

    @Operation(summary = "문의 수정", description = "문의 ID로 내용을 수정합니다. (ADMIN 전용)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "수정된 문의",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = QnaDescription.class)))
    @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content)
    @ApiResponse(responseCode = "404", description = "문의 없음", content = @Content)
    ResponseEntity<QnaDescription> updateQna(@RequestBody EditQnaRequest request,
                                             @PathVariable Long id, Principal principal);

    @Operation(summary = "문의 삭제", description = "문의 ID로 삭제합니다. (ADMIN 전용)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content)
    @ApiResponse(responseCode = "404", description = "문의 없음", content = @Content)
    ResponseEntity<Void> deleteQna(@PathVariable Long id);
}
