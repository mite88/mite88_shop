package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.dto.QnaPageResponse;
import io.mite88.mite88shop.posts.service.QnaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static io.mite88.mite88shop.global.code.ResponseCode.POST_NOT_FOUND;
import static io.mite88.mite88shop.global.code.ResponseCode.UNAUTHORIZED_ACCESS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QnaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QnaService qnaService;

    private final String BASE_URL = "/qna";

    @Test
    @DisplayName("문의 작성 성공 - ADMIN")
    void saveQna_Success() throws Exception {
        EditQnaRequest request = new EditQnaRequest("Test Title Qna", "Test Content Qna");
        QnaDescription expectedResponse = new QnaDescription(1L, "Test Title Qna", "Test Content Qna", "admin", LocalDateTime.now());

        when(qnaService.save(any(EditQnaRequest.class), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title Qna"))
                .andExpect(jsonPath("$.authorName").value("admin"));

        verify(qnaService, times(1)).save(any(EditQnaRequest.class), anyString());
    }

    @Test
    @DisplayName("문의 작성 실패 - 인증되지 않은 사용자")
    void saveQna_Unauthorized() throws Exception {
        EditQnaRequest request = new EditQnaRequest("Test Title Qna", "Test Content Qna");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(UNAUTHORIZED_ACCESS.getCode()));

        verify(qnaService, times(0)).save(any(EditQnaRequest.class), anyString());
    }

    @Test
    @DisplayName("문의 작성 실패 - 일반 회원 (ADMIN 아님)")
    void saveQna_Forbidden_Member() throws Exception {
        EditQnaRequest request = new EditQnaRequest("Test Title Qna", "Test Content Qna");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .with(user("member").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(qnaService, times(0)).save(any(EditQnaRequest.class), anyString());
    }

    @Test
    @DisplayName("단일 문의 조회 성공")
    void findById_Success() throws Exception {
        Long qnaId = 1L;
        QnaDescription expectedResponse = new QnaDescription(qnaId, "Found Title", "Found Content", "admin", LocalDateTime.now());

        when(qnaService.findById(qnaId)).thenReturn(expectedResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", qnaId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(qnaId))
                .andExpect(jsonPath("$.title").value("Found Title"));

        verify(qnaService, times(1)).findById(qnaId);
    }

    @Test
    @DisplayName("단일 문의 조회 실패 - 없음")
    void findById_NotFound() throws Exception {
        Long qnaId = 99L;
        when(qnaService.findById(qnaId)).thenThrow(new BusinessException(POST_NOT_FOUND));

        mockMvc.perform(get(BASE_URL + "/{id}", qnaId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(POST_NOT_FOUND.getCode()));

        verify(qnaService, times(1)).findById(qnaId);
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 성공 (기본 0페이지)")
    void findAll_Success() throws Exception {
        List<QnaDescription> list = List.of(
                new QnaDescription(1L, "Title 1", "Content 1", "admin", LocalDateTime.now()),
                new QnaDescription(2L, "Title 2", "Content 2", "admin", LocalDateTime.now())
        );
        QnaPageResponse pageResponse = new QnaPageResponse(list, 1, 2L, 0);

        when(qnaService.findAll(0)).thenReturn(pageResponse);

        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));

        verify(qnaService, times(1)).findAll(0);
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 - 특정 페이지")
    void findAll_WithPage() throws Exception {
        QnaPageResponse pageResponse = new QnaPageResponse(List.of(), 3, 25L, 2);

        when(qnaService.findAll(2)).thenReturn(pageResponse);

        mockMvc.perform(get(BASE_URL).param("page", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(qnaService, times(1)).findAll(2);
    }

    @Test
    @DisplayName("문의 수정 성공 - ADMIN")
    void updateQna_Success() throws Exception {
        Long qnaId = 1L;
        EditQnaRequest request = new EditQnaRequest("Updated Title Qna", "Updated Content Qna");
        QnaDescription expectedResponse = new QnaDescription(qnaId, "Updated Title Qna", "Updated Content Qna", "admin", LocalDateTime.now());

        when(qnaService.updateQna(any(EditQnaRequest.class), anyLong(), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}", qnaId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title Qna"));

        verify(qnaService, times(1)).updateQna(any(EditQnaRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("문의 수정 실패 - 인증되지 않은 사용자")
    void updateQna_Unauthorized() throws Exception {
        EditQnaRequest request = new EditQnaRequest("Updated Title Qna", "Updated Content Qna");

        mockMvc.perform(patch(BASE_URL + "/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(qnaService, times(0)).updateQna(any(EditQnaRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("문의 삭제 성공 - ADMIN")
    void deleteQna_Success() throws Exception {
        Long qnaId = 1L;
        doNothing().when(qnaService).deleteQna(qnaId);

        mockMvc.perform(delete(BASE_URL + "/{id}", qnaId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(qnaService, times(1)).deleteQna(qnaId);
    }

    @Test
    @DisplayName("문의 삭제 실패 - 인증되지 않은 사용자")
    void deleteQna_Unauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(qnaService, times(0)).deleteQna(anyLong());
    }
}
