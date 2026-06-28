package io.mite88.mite88shop.posts.dto;

import java.time.LocalDateTime;

public record CommentDescription(
        Long id,
        Long postId,
        String content,
        String authorName,
        LocalDateTime createdAt,
        boolean isAdminAuthor
) {}
