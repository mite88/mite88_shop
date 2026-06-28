package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.dto.PostPageResponse;
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

public interface PostApiDocs {

    @Operation(
            summary = "게시글 작성",
            description = "새 게시글을 작성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "작성된 게시글 정보",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostDescription.class)
            )
    )
    ResponseEntity<PostDescription> savePost(
            @RequestBody EditPostRequest request,
            Principal principal
    );

    @Operation(
            summary = "게시글 단건 조회",
            description = "게시글 ID로 게시글 하나를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회된 게시글 정보",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostDescription.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<PostDescription> findById(@PathVariable Long id);

    @Operation(
            summary = "문의 게시글 페이징 조회",
            description = "page 파라미터(0-indexed)로 10개씩 최신순 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "페이징된 게시글 목록",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostPageResponse.class)
            )
    )
    ResponseEntity<PostPageResponse> findAll(@RequestParam(defaultValue = "0") int page);

    @Operation(
            summary = "게시글 수정",
            description = "게시글 ID로 게시글 내용을 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "수정된 게시글 정보",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostDescription.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "게시글 수정 권한 없음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<PostDescription> updatePost(
            @RequestBody EditPostRequest request,
            @PathVariable Long id,
            Principal principal
    );

    @Operation(
            summary = "게시글 삭제",
            description = "게시글 ID로 게시글을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(
            responseCode = "204",
            description = "게시글 삭제 성공",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "게시글 삭제 권한 없음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<Void> deletePost(@PathVariable Long id);
}
