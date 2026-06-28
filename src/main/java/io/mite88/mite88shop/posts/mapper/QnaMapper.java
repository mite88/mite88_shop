package io.mite88.mite88shop.posts.mapper;

import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.entity.Qna;

public class QnaMapper {

    public static QnaDescription toDescription(Qna entity) {
        return new QnaDescription(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthor().getUsername(),
                entity.getCreatedAt()
        );
    }
}
