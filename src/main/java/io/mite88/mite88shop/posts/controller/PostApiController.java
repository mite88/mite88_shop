package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.dto.PostPageResponse;
import io.mite88.mite88shop.posts.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class PostApiController implements PostApiDocs{

    private final PostService service;


    /**
     * 게시글 작성
     */
    @Override
    @PostMapping
    public ResponseEntity<PostDescription> savePost(
            @RequestBody EditPostRequest request,
            Principal principal
    ) {
        PostDescription postDescription = service.save(request, principal.getName());

        return ResponseEntity.ok(postDescription);

    }

    /**
     * 게시글 단건 조회
     */
    @Override
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<PostDescription> findById(@PathVariable Long id) {
        PostDescription postDescription = service.findById(id);
        return ResponseEntity.ok(postDescription);
    }

    /**
     * 문의 게시글 페이징 조회 (기본 1페이지, 10개)
     */
    @Override
    @GetMapping
    public ResponseEntity<PostPageResponse> findAll(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(service.findAll(page));
    }

    /**
     * 게시글 수정 - 작성자 본인만 가능
     */
    @Override
    @PatchMapping("/{id:[0-9]+}")
    public ResponseEntity<PostDescription> updatePost(
            @RequestBody EditPostRequest request,
            @PathVariable Long id,
            Principal principal
    ) {

        PostDescription description = service.updatePost(request, id, principal.getName());

        return ResponseEntity.ok(description);
    }

    /**
     * 게시글 삭제
     */
    @Override
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id
    ) {
        service.deletePost(id);
        return ResponseEntity.noContent()
                .build();
    }

}
