package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostApiController implements PostApiDocs{

    private final PostService service;


    @Override
    @PostMapping
    public ResponseEntity<PostDescription> savePost(
            @RequestBody EditPostRequest request,
            Principal principal
    ) {
        PostDescription postDescription = service.save(request, principal.getName());

        return ResponseEntity.ok(postDescription);

    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PostDescription> findById(@PathVariable Long id) {
        PostDescription postDescription = service.findById(id);
        return ResponseEntity.ok(postDescription);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PostDescription>> findAll() {
        List<PostDescription> postDescriptions = service.findAll();
        return ResponseEntity.ok(postDescriptions);
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<PostDescription> updatePost(
            @RequestBody EditPostRequest request,
            @PathVariable Long id,
            Principal principal
    ) {

        PostDescription description = service.updatePost(request, id, principal.getName());

        return ResponseEntity.ok(description);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id
    ) {
        service.deletePost(id);
        return ResponseEntity.noContent()
                .build();
    }

}
