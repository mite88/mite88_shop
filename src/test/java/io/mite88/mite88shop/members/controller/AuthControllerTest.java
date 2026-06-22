package io.mite88.mite88shop.members.controller;

import io.mite88.mite88shop.members.dto.LoginRequest;
import io.mite88.mite88shop.members.dto.MemberSaveRequest;
import io.mite88.mite88shop.members.dto.RefreshRequest;
import io.mite88.mite88shop.members.dto.TokenResponse;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import io.mite88.mite88shop.members.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-config.properties")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    private Map<String, String> tokenStore;

    private static final String BASE_URL = "/api/v1/auth";

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        tokenStore = new HashMap<>();
        doAnswer(inv -> { tokenStore.put(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(refreshTokenService).save(anyString(), anyString());
        when(refreshTokenService.find(anyString())).thenAnswer(inv ->
                Optional.ofNullable(tokenStore.get(inv.getArgument(0, String.class))));
        doAnswer(inv -> { tokenStore.remove(inv.getArgument(0)); return null; })
                .when(refreshTokenService).delete(anyString());
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() throws Exception {
        MemberSaveRequest request = new MemberSaveRequest("testuser", "password123", "test@example.com");

        mockMvc.perform(post(BASE_URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        String rawPassword = "password123";
        memberRepository.save(Member.builder()
                .username("loginuser")
                .password(passwordEncoder.encode(rawPassword))
                .email("login@example.com")
                .build());

        LoginRequest request = new LoginRequest("loginuser", rawPassword);

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_UserNotFound_ShouldReturnResponse() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A002"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_InvalidPassword_ShouldReturnResponse() throws Exception {
        String rawPassword = "password123";
        memberRepository.save(Member.builder()
                .username("wrongpassuser")
                .password(passwordEncoder.encode(rawPassword))
                .email("wrongpass@example.com")
                .build());

        LoginRequest request = new LoginRequest("wrongpassuser", "wrongpassword");

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("A001"))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_Success() throws Exception {
        String rawPassword = "password123";
        memberRepository.save(Member.builder()
                .username("refreshuser")
                .password(passwordEncoder.encode(rawPassword))
                .email("refresh@example.com")
                .build());

        LoginRequest loginRequest = new LoginRequest("refreshuser", rawPassword);
        String loginResponseContent = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(loginResponseContent, TokenResponse.class);
        String refreshToken = tokenResponse.refreshToken();

        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        mockMvc.perform(post(BASE_URL + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        String rawPassword = "password123";
        memberRepository.save(Member.builder()
                .username("logoutuser")
                .password(passwordEncoder.encode(rawPassword))
                .email("logout@example.com")
                .build());

        LoginRequest loginRequest = new LoginRequest("logoutuser", rawPassword);
        String loginResponseContent = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(loginResponseContent, TokenResponse.class);
        String accessToken = tokenResponse.accessToken();

        mockMvc.perform(post(BASE_URL + "/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
