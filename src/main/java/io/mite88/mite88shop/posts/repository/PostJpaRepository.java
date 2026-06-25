package io.mite88.mite88shop.posts.repository;

import io.mite88.mite88shop.posts.entity.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Posts, Long> {
}
