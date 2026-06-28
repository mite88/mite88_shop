package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.CommentCreateRequest;
import io.mite88.mite88shop.posts.dto.CommentDescription;
import io.mite88.mite88shop.posts.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/qna/{postId:[0-9]+}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDescription> save(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.save(postId, request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CommentDescription>> findAll(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findByPostId(postId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal
    ) {
        commentService.delete(postId, commentId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
