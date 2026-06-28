package io.mite88.mite88shop.posts.entity;

import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    @Setter
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member author;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void update(EditQnaRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.updatedAt = LocalDateTime.now();
    }
}
