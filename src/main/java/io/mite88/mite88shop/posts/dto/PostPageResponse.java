package io.mite88.mite88shop.posts.dto;

import java.util.List;

public record PostPageResponse(
        List<PostDescription> content,
        int totalPages,
        long totalElements,
        int number
) {}
