package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.dto.QnaPageResponse;
import io.mite88.mite88shop.posts.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController implements QnaApiDocs {

    private final QnaService service;

    @Override
    @PostMapping
    public ResponseEntity<QnaDescription> saveQna(
            @RequestBody EditQnaRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(service.save(request, principal.getName()));
    }

    @Override
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<QnaDescription> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<QnaPageResponse> findAll(
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(service.findAll(page));
    }

    @Override
    @PatchMapping("/{id:[0-9]+}")
    public ResponseEntity<QnaDescription> updateQna(
            @RequestBody EditQnaRequest request,
            @PathVariable Long id,
            Principal principal
    ) {
        return ResponseEntity.ok(service.updateQna(request, id, principal.getName()));
    }

    @Override
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteQna(@PathVariable Long id) {
        service.deleteQna(id);
        return ResponseEntity.noContent().build();
    }
}
