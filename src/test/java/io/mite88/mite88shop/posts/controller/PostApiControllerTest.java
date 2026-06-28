package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.dto.PostPageResponse;
import io.mite88.mite88shop.posts.service.PostService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    private final String BASE_URL = "/qna";

    @Test
    @DisplayName("문의 작성 성공 - ADMIN")
    void savePost_Success() throws Exception {
        EditPostRequest request = new EditPostRequest("Test Title", "Test Content");
        PostDescription expectedResponse = new PostDescription(1L, "Test Title", "Test Content", "admin", LocalDateTime.now());

        when(postService.save(any(EditPostRequest.class), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.authorName").value("admin"));

        verify(postService, times(1)).save(any(EditPostRequest.class), anyString());
    }

    @Test
    @DisplayName("문의 작성 실패 - 인증되지 않은 사용자")
    void savePost_Unauthorized() throws Exception {
        EditPostRequest request = new EditPostRequest("Test Title", "Test Content");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(UNAUTHORIZED_ACCESS.getCode()));

        verify(postService, times(0)).save(any(EditPostRequest.class), anyString());
    }

    @Test
    @DisplayName("문의 작성 실패 - 일반 회원 (ADMIN 아님)")
    void savePost_Forbidden_Member() throws Exception {
        EditPostRequest request = new EditPostRequest("Test Title", "Test Content");

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .with(user("member").roles("MEMBER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(postService, times(0)).save(any(EditPostRequest.class), anyString());
    }

    @Test
    @DisplayName("단일 문의 조회 성공")
    void findById_Success() throws Exception {
        Long postId = 1L;
        PostDescription expectedResponse = new PostDescription(postId, "Found Title", "Found Content", "admin", LocalDateTime.now());

        when(postService.findById(postId)).thenReturn(expectedResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Found Title"));

        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("단일 문의 조회 실패 - 없음")
    void findById_NotFound() throws Exception {
        Long postId = 99L;
        when(postService.findById(postId)).thenThrow(new BusinessException(POST_NOT_FOUND));

        mockMvc.perform(get(BASE_URL + "/{id}", postId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(POST_NOT_FOUND.getCode()));

        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 성공 (기본 0페이지)")
    void findAll_Success() throws Exception {
        List<PostDescription> posts = List.of(
                new PostDescription(1L, "Title 1", "Content 1", "admin", LocalDateTime.now()),
                new PostDescription(2L, "Title 2", "Content 2", "admin", LocalDateTime.now())
        );
        PostPageResponse pageResponse = new PostPageResponse(posts, 1, 2L, 0);

        when(postService.findAll(0)).thenReturn(pageResponse);

        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));

        verify(postService, times(1)).findAll(0);
    }

    @Test
    @DisplayName("문의 목록 페이징 조회 - 특정 페이지")
    void findAll_WithPage() throws Exception {
        PostPageResponse pageResponse = new PostPageResponse(List.of(), 3, 25L, 2);

        when(postService.findAll(2)).thenReturn(pageResponse);

        mockMvc.perform(get(BASE_URL).param("page", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(postService, times(1)).findAll(2);
    }

    @Test
    @DisplayName("문의 수정 성공 - ADMIN")
    void updatePost_Success() throws Exception {
        Long postId = 1L;
        EditPostRequest request = new EditPostRequest("Updated Title", "Updated Content");
        PostDescription expectedResponse = new PostDescription(postId, "Updated Title", "Updated Content", "admin", LocalDateTime.now());

        when(postService.updatePost(any(EditPostRequest.class), anyLong(), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}", postId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(postService, times(1)).updatePost(any(EditPostRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("문의 수정 실패 - 인증되지 않은 사용자")
    void updatePost_Unauthorized() throws Exception {
        EditPostRequest request = new EditPostRequest("Updated Title", "Updated Content");

        mockMvc.perform(patch(BASE_URL + "/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(postService, times(0)).updatePost(any(EditPostRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("문의 삭제 성공 - ADMIN")
    void deletePost_Success() throws Exception {
        Long postId = 1L;
        doNothing().when(postService).deletePost(postId);

        mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(postId);
    }

    @Test
    @DisplayName("문의 삭제 실패 - 인증되지 않은 사용자")
    void deletePost_Unauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(postService, times(0)).deletePost(anyLong());
    }
}
