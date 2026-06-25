package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.LoginRequest;
import io.mite88.mite88shop.members.dto.MemberDetails;
import io.mite88.mite88shop.members.dto.TokenResponse;
import io.mite88.mite88shop.members.dto.Role; // MemberRole 대신 Role import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberService memberService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberDetails memberDetails;
    private LoginRequest loginRequest;
    private String username = "testuser";
    private String password = "password123";
    private String encodedPassword = "encodedPassword";
    private String accessToken = "mockAccessToken";
    private String refreshToken = "mockRefreshToken";

    @BeforeEach
    void setUp() {
        memberDetails = new MemberDetails(username, encodedPassword, Role.MEMBER); // MemberRole.USER 대신 Role.MEMBER 사용
        loginRequest = new LoginRequest(username, password);

        // @Value 필드 모의 설정
        ReflectionTestUtils.setField(authService, "accessExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        when(memberService.loadUserByUsername(username)).thenReturn(memberDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.issue(anyLong(), anyMap())).thenReturn(accessToken);
        when(jwtTokenProvider.issueRefreshToken(anyLong(), anyString())).thenReturn(refreshToken);
        doNothing().when(refreshTokenService).save(anyString(), anyString());

        TokenResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(memberService, times(1)).loadUserByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtTokenProvider, times(1)).issue(anyLong(), anyMap());
        verify(jwtTokenProvider, times(1)).issueRefreshToken(anyLong(), anyString());
        verify(refreshTokenService, times(1)).save(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_UserNotFound_ThrowsBusinessException() {
        when(memberService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.USER_NOT_FOUND); // BusinessException에서 UsernameNotFoundException을 USER_NOT_FOUND로 매핑한다고 가정
        verify(memberService, times(1)).loadUserByUsername(username);
        verify(passwordEncoder, times(0)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_InvalidPassword_ThrowsBusinessException() {
        when(memberService.loadUserByUsername(username)).thenReturn(memberDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginRequest));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.INVALID_PASSWORD);
        verify(memberService, times(1)).loadUserByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtTokenProvider, times(0)).issue(anyLong(), anyMap());
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_Success() {
        String oldRefreshToken = "oldRefreshToken";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        when(jwtTokenProvider.validate(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getClaims(oldRefreshToken)).thenReturn(claims);
        when(refreshTokenService.find(username)).thenReturn(Optional.of(oldRefreshToken));
        when(memberService.loadUserByUsername(username)).thenReturn(memberDetails);
        when(jwtTokenProvider.issue(anyLong(), anyMap())).thenReturn(accessToken);
        when(jwtTokenProvider.issueRefreshToken(anyLong(), anyString())).thenReturn(refreshToken);
        doNothing().when(refreshTokenService).save(anyString(), anyString());

        TokenResponse response = authService.refresh(oldRefreshToken);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(jwtTokenProvider, times(1)).validate(oldRefreshToken);
        verify(jwtTokenProvider, times(1)).getClaims(oldRefreshToken);
        verify(refreshTokenService, times(1)).find(username);
        verify(memberService, times(1)).loadUserByUsername(username);
        verify(jwtTokenProvider, times(1)).issue(anyLong(), anyMap());
        verify(jwtTokenProvider, times(1)).issueRefreshToken(anyLong(), anyString());
        verify(refreshTokenService, times(1)).save(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void refresh_InvalidToken_ThrowsBusinessException() {
        String invalidRefreshToken = "invalidToken";
        when(jwtTokenProvider.validate(invalidRefreshToken)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refresh(invalidRefreshToken));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.INVALID_REFRESH_TOKEN);
        verify(jwtTokenProvider, times(1)).validate(invalidRefreshToken);
        verify(jwtTokenProvider, times(0)).getClaims(anyString());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료 또는 없음")
    void refresh_RefreshTokenExpired_ThrowsBusinessException() {
        String oldRefreshToken = "oldRefreshToken";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        when(jwtTokenProvider.validate(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getClaims(oldRefreshToken)).thenReturn(claims);
        when(refreshTokenService.find(username)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refresh(oldRefreshToken));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.REFRESH_TOKEN_EXPIRED);
        verify(jwtTokenProvider, times(1)).validate(oldRefreshToken);
        verify(jwtTokenProvider, times(1)).getClaims(oldRefreshToken);
        verify(refreshTokenService, times(1)).find(username);
        verify(memberService, times(0)).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 불일치")
    void refresh_RefreshTokenMismatch_ThrowsBusinessException() {
        String oldRefreshToken = "oldRefreshToken";
        String storedRefreshToken = "differentRefreshToken"; // 저장된 토큰이 다름
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        when(jwtTokenProvider.validate(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getClaims(oldRefreshToken)).thenReturn(claims);
        when(refreshTokenService.find(username)).thenReturn(Optional.of(storedRefreshToken));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refresh(oldRefreshToken));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.REFRESH_TOKEN_MISMATCH);
        verify(jwtTokenProvider, times(1)).validate(oldRefreshToken);
        verify(jwtTokenProvider, times(1)).getClaims(oldRefreshToken);
        verify(refreshTokenService, times(1)).find(username);
        verify(memberService, times(0)).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        doNothing().when(refreshTokenService).delete(username);

        authService.logout(username);

        verify(refreshTokenService, times(1)).delete(username);
    }
}
