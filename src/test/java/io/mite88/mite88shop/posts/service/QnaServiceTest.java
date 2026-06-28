package io.mite88.mite88shop.posts.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.Role;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.dto.QnaPageResponse;
import io.mite88.mite88shop.posts.entity.Qna;
import io.mite88.mite88shop.posts.repository.QnaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {

    @InjectMocks
    private QnaService qnaService;

    @Mock
    private QnaRepository qnaRepository;

    @Mock
    private MemberService memberService;

    private Member testMember;
    private Qna testQna;
    private EditQnaRequest editQnaRequest;
    private final String username = "testuser";
    private final Long qnaId = 1L;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .username(username)
                .password("encodedPassword")
                .email("test@example.com")
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testQna = Qna.builder()
                .title("Original Title")
                .content("Original Content")
                .author(testMember)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testQna, "id", qnaId);

        editQnaRequest = new EditQnaRequest("New Title Xyz", "New Content Updated");
    }

    @Test
    @DisplayName("문의 저장 성공")
    void save_Success() {
        when(memberService.findByUsername(username)).thenReturn(testMember);
        when(qnaRepository.save(any(Qna.class))).thenReturn(testQna);

        QnaDescription result = qnaService.save(editQnaRequest, username);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(testQna.getTitle());
        assertThat(result.content()).isEqualTo(testQna.getContent());

        verify(memberService, times(1)).findByUsername(username);
        verify(qnaRepository, times(1)).save(any(Qna.class));
    }

    @Test
    @DisplayName("ID로 문의 조회 성공")
    void findById_Success() {
        when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(testQna));

        QnaDescription result = qnaService.findById(qnaId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(qnaId);
        assertThat(result.title()).isEqualTo(testQna.getTitle());
        assertThat(result.content()).isEqualTo(testQna.getContent());
        assertThat(result.authorName()).isEqualTo(username);

        verify(qnaRepository, times(1)).findById(qnaId);
    }

    @Test
    @DisplayName("ID로 문의 조회 실패 - 없음")
    void findById_NotFound() {
        when(qnaRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> qnaService.findById(99L));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.POST_NOT_FOUND);
        verify(qnaRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 성공")
    void findAll_Success() {
        Qna anotherQna = Qna.builder()
                .title("Another Title")
                .content("Another Content")
                .author(testMember)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(anotherQna, "id", 2L);

        List<Qna> list = List.of(testQna, anotherQna);
        PageImpl<Qna> page = new PageImpl<>(list, PageRequest.of(0, 10), 2);

        when(qnaRepository.findAll(any(Pageable.class))).thenReturn(page);

        QnaPageResponse result = qnaService.findAll(0);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.number()).isEqualTo(0);

        verify(qnaRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 - 빈 목록")
    void findAll_Empty() {
        PageImpl<Qna> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(qnaRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        QnaPageResponse result = qnaService.findAll(0);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);

        verify(qnaRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("문의 수정 성공")
    void updateQna_Success() {
        when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(testQna));

        QnaDescription result = qnaService.updateQna(editQnaRequest, qnaId, username);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(qnaId);
        assertThat(result.title()).isEqualTo(editQnaRequest.title());
        assertThat(result.content()).isEqualTo(editQnaRequest.content());

        verify(qnaRepository, times(1)).findById(qnaId);
    }

    @Test
    @DisplayName("문의 수정 실패 - 없음")
    void updateQna_NotFound() {
        when(qnaRepository.findById(anyLong())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> qnaService.updateQna(editQnaRequest, 99L, username));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.POST_NOT_FOUND);
        verify(qnaRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("문의 수정 실패 - 권한 없음")
    void updateQna_Unauthorized() {
        Member otherMember = Member.builder()
                .username("otheruser")
                .password("encodedPassword")
                .email("other@example.com")
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(otherMember, "id", 2L);
        testQna.setAuthor(otherMember);

        when(qnaRepository.findById(qnaId)).thenReturn(Optional.of(testQna));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> qnaService.updateQna(editQnaRequest, qnaId, username));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.UNAUTHORIZED_POST_UPDATE);
        verify(qnaRepository, times(1)).findById(qnaId);
    }

    @Test
    @DisplayName("문의 삭제 성공")
    void deleteQna_Success() {
        doNothing().when(qnaRepository).deleteById(qnaId);

        qnaService.deleteQna(qnaId);

        verify(qnaRepository, times(1)).deleteById(qnaId);
    }
}
