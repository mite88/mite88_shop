package io.mite88.mite88shop.posts.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.Role;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.posts.dto.CommentCreateRequest;
import io.mite88.mite88shop.posts.dto.CommentDescription;
import io.mite88.mite88shop.posts.entity.Comment;
import io.mite88.mite88shop.posts.entity.Posts;
import io.mite88.mite88shop.posts.repository.CommentRepository;
import io.mite88.mite88shop.posts.repository.PostJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostJpaRepository postRepository;
    private final MemberService memberService;

    @Transactional
    public CommentDescription save(Long postId, CommentCreateRequest request, String username) {
        Posts post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.POST_NOT_FOUND));
        Member member = memberService.findByUsername(username);

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(request.content())
                .build();

        Comment saved = commentRepository.save(comment);
        return toDescription(saved);
    }

    public List<CommentDescription> findByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(this::toDescription).toList();
    }

    @Transactional
    public void delete(Long postId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ResponseCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ResponseCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED_COMMENT_DELETE);
        }

        commentRepository.deleteById(commentId);
    }

    private CommentDescription toDescription(Comment comment) {
        return new CommentDescription(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt(),
                comment.getAuthor().getRole() == Role.ADMIN
        );
    }
}
