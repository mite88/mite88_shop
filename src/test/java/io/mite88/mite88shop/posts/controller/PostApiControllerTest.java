package io.mite88.mite88shop.posts.controller;

import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.mite88.mite88shop.mite88shop.global.code.ResponseCode.POST_NOT_FOUND;
import static io.mite88.mite88shop.mite88shop.global.code.ResponseCode.UNAUTHORIZED_ACCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // Import user
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

    private final String BASE_URL = "/posts";

    @Test
    @DisplayName("게시글 생성 성공")
    void savePost_Success() throws Exception {
        EditPostRequest request = new EditPostRequest("Test Title", "Test Content");
        PostDescription expectedResponse = new PostDescription(1L, "Test Title", "Test Content", "testuser", LocalDateTime.now());

        when(postService.save(any(EditPostRequest.class), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post(BASE_URL)
                        .with(csrf())
                        .with(user("testuser").roles("USER")) // Explicitly set user for the request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.authorName").value("testuser")); // Corrected jsonPath

        verify(postService, times(1)).save(any(EditPostRequest.class), anyString());
    }

    @Test
    @DisplayName("게시글 생성 실패 - 인증되지 않은 사용자")
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
    @DisplayName("단일 게시글 조회 성공")
    void findById_Success() throws Exception {
        Long postId = 1L;
        PostDescription expectedResponse = new PostDescription(postId, "Found Title", "Found Content", "author1", LocalDateTime.now());

        when(postService.findById(postId)).thenReturn(expectedResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Found Title"));

        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("단일 게시글 조회 실패 - 게시글 없음")
    void findById_NotFound() throws Exception {
        Long postId = 99L;
        when(postService.findById(postId)).thenThrow(new BusinessException(POST_NOT_FOUND));

        mockMvc.perform(get(BASE_URL + "/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(POST_NOT_FOUND.getCode()));

        verify(postService, times(1)).findById(postId);
    }

    @Test
    @DisplayName("모든 게시글 조회 성공")
    void findAll_Success() throws Exception {
        List<PostDescription> expectedResponses = Arrays.asList(
                new PostDescription(1L, "Title 1", "Content 1", "author1", LocalDateTime.now()),
                new PostDescription(2L, "Title 2", "Content 2", "author2", LocalDateTime.now())
        );

        when(postService.findAll()).thenReturn(expectedResponses);

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].title").value("Title 2"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(postService, times(1)).findAll();
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() throws Exception {
        Long postId = 1L;
        EditPostRequest request = new EditPostRequest("Updated Title", "Updated Content");
        PostDescription expectedResponse = new PostDescription(postId, "Updated Title", "Updated Content", "testuser", LocalDateTime.now());

        when(postService.updatePost(any(EditPostRequest.class), anyLong(), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}", postId)
                        .with(csrf())
                        .with(user("testuser").roles("USER")) // Explicitly set user for the request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(postService, times(1)).updatePost(any(EditPostRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 인증되지 않은 사용자")
    void updatePost_Unauthorized() throws Exception {
        Long postId = 1L;
        EditPostRequest request = new EditPostRequest("Updated Title", "Updated Content");

        mockMvc.perform(patch(BASE_URL + "/{id}", postId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(UNAUTHORIZED_ACCESS.getCode()));
        verify(postService, times(0)).updatePost(any(EditPostRequest.class), anyLong(), anyString());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() throws Exception {
        Long postId = 1L;
        doNothing().when(postService).deletePost(postId);

        mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                        .with(csrf())
                        .with(user("testuser").roles("USER"))) // Explicitly set user for the request
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(postId);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 인증되지 않은 사용자")
    void deletePost_Unauthorized() throws Exception {
        Long postId = 1L;

        // Removed the line causing compilation error: when(postService.deletePost(anyLong())).thenThrow(new UnauthorizedAccessException());

        mockMvc.perform(delete(BASE_URL + "/{id}", postId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(UNAUTHORIZED_ACCESS.getCode()));

        verify(postService, times(0)).deletePost(anyLong());
    }
}