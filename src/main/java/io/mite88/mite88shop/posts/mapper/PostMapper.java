package io.mite88.mite88shop.posts.mapper;

import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.entity.Posts;

public class PostMapper {

    public static PostDescription toDescription(Posts entity) {
        return new PostDescription(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthor().getUsername(),
                entity.getCreatedAt()
        );
    }

}
