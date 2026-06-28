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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostJpaRepository postRepository;

    @Mock
    private MemberService memberService;

    private Member adminMember;
    private Posts testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        adminMember = Member.builder()
                .username("admin")
                .password("encoded")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();
        ReflectionTestUtils.setField(adminMember, "id", 1L);

        testPost = Posts.builder()
                .title("문의 제목")
                .content("문의 내용")
                .author(adminMember)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testPost, "id", 1L);

        testComment = Comment.builder()
                .post(testPost)
                .author(adminMember)
                .content("답변 내용")
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testComment, "id", 1L);
    }

    @Test
    @DisplayName("답변 저장 성공")
    void save_Success() {
        CommentCreateRequest request = new CommentCreateRequest("답변 내용");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(memberService.findByUsername("admin")).thenReturn(adminMember);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDescription result = commentService.save(1L, request, "admin");

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("답변 내용");
        assertThat(result.authorName()).isEqualTo("admin");
        assertThat(result.isAdminAuthor()).isTrue();

        verify(postRepository, times(1)).findById(1L);
        verify(memberService, times(1)).findByUsername("admin");
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("답변 저장 실패 - 게시글 없음")
    void save_PostNotFound() {
        CommentCreateRequest request = new CommentCreateRequest("답변 내용");

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.save(99L, request, "admin"));

        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글의 답변 목록 조회 성공")
    void findByPostId_Success() {
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(testComment));

        List<CommentDescription> result = commentService.findByPostId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).authorName()).isEqualTo("admin");
        assertThat(result.get(0).isAdminAuthor()).isTrue();

        verify(commentRepository, times(1)).findByPostIdOrderByCreatedAtAsc(1L);
    }

    @Test
    @DisplayName("답변 삭제 성공")
    void delete_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        commentService.delete(1L, 1L, "admin");

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("답변 삭제 실패 - 답변 없음")
    void delete_CommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.delete(1L, 99L, "admin"));

        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("답변 삭제 실패 - 작성자 불일치")
    void delete_UnauthorizedDelete() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.delete(1L, 1L, "other"));

        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.UNAUTHORIZED_COMMENT_DELETE);
    }
}
