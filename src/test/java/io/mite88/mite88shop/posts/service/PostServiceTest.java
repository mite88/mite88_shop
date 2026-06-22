package io.mite88.mite88shop.posts.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.dto.Role; // MemberRole 대신 Role import
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.entity.Posts;
import io.mite88.mite88shop.posts.repository.PostJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // ReflectionTestUtils import

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException; // 더 이상 사용하지 않으므로 제거 가능
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostJpaRepository postJpaRepository;

    @Mock
    private MemberService memberService;

    private Member testMember;
    private Posts testPost;
    private EditPostRequest editPostRequest;
    private String username = "testuser";
    private Long postId = 1L;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                // .id(1L) // id는 @GeneratedValue이므로 빌더에서 설정하지 않습니다.
                .username(username)
                .password("encodedPassword")
                .email("test@example.com")
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L); // Reflection을 사용하여 id 설정

        testPost = Posts.builder()
                // .id(postId) // id는 @GeneratedValue이므로 빌더에서 설정하지 않습니다.
                .title("Original Title")
                .content("Original Content")
                .author(testMember)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testPost, "id", postId); // Reflection을 사용하여 id 설정

        editPostRequest = new EditPostRequest("New Title", "New Content");
    }

    @Test
    @DisplayName("게시글 저장 성공")
    void save_Success() {
        when(memberService.findByUsername(username)).thenReturn(testMember);
        when(postJpaRepository.save(any(Posts.class))).thenReturn(testPost);

        PostDescription result = postService.save(editPostRequest, username);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(testPost.getTitle());
        assertThat(result.content()).isEqualTo(testPost.getContent());
        //assertThat(result.author()).isEqualTo(username);

        verify(memberService, times(1)).findByUsername(username);
        verify(postJpaRepository, times(1)).save(any(Posts.class));
    }

    @Test
    @DisplayName("ID로 게시글 조회 성공")
    void findById_Success() {
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(testPost));

        PostDescription result = postService.findById(postId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo(testPost.getTitle());
        assertThat(result.content()).isEqualTo(testPost.getContent());
        assertThat(result.authorName()).isEqualTo(username);

        verify(postJpaRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("ID로 게시글 조회 실패 - 게시글 없음")
    void findById_NotFound_ThrowsBusinessException() { // 테스트 이름 변경
        when(postJpaRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> postService.findById(99L)); // BusinessException 검증

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.POST_NOT_FOUND); // ResponseCode 검증
        verify(postJpaRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("모든 게시글 조회 성공")
    void findAll_Success() {
        Posts anotherPost = Posts.builder()
                .title("Another Title")
                .content("Another Content")
                .author(testMember)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(anotherPost, "id", 2L); // Reflection을 사용하여 id 설정

        List<Posts> postsList = Arrays.asList(testPost, anotherPost);

        when(postJpaRepository.findAll()).thenReturn(postsList);

        List<PostDescription> result = postService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo(testPost.getTitle());
        assertThat(result.get(1).title()).isEqualTo(anotherPost.getTitle());

        verify(postJpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("모든 게시글 조회 성공 - 게시글 없음")
    void findAll_EmptyList() {
        when(postJpaRepository.findAll()).thenReturn(Collections.emptyList());

        List<PostDescription> result = postService.findAll();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(postJpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(testPost));

        PostDescription result = postService.updatePost(editPostRequest, postId, username);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo(editPostRequest.title());
        assertThat(result.content()).isEqualTo(editPostRequest.content());
        assertThat(result.authorName()).isEqualTo(username);

        verify(postJpaRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void updatePost_NotFound_ThrowsBusinessException() { // 테스트 이름 변경
        when(postJpaRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> postService.updatePost(editPostRequest, 99L, username)); // BusinessException 검증

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.POST_NOT_FOUND); // ResponseCode 검증
        verify(postJpaRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePost_Unauthorized_ThrowsBusinessException() {
        Member otherMember = Member.builder()
                .username("otheruser")
                .password("encodedPassword")
                .email("other@example.com")
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(otherMember, "id", 2L); // Reflection을 사용하여 id 설정

        testPost.setAuthor(otherMember); // 다른 사용자가 작성한 게시글로 설정

        when(postJpaRepository.findById(postId)).thenReturn(Optional.of(testPost));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> postService.updatePost(editPostRequest, postId, username)); // testuser가 수정 시도

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.UNAUTHORIZED_POST_UPDATE);
        verify(postJpaRepository, times(1)).findById(postId);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        doNothing().when(postJpaRepository).deleteById(postId);

        postService.deletePost(postId);

        verify(postJpaRepository, times(1)).deleteById(postId);
    }
}