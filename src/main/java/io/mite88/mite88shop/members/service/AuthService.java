package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.LoginRequest;
import io.mite88.mite88shop.members.dto.MemberDetails;
import io.mite88.mite88shop.members.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${custom.jwt.expiration}")
    private long accessExpiration;

    @Value("${custom.jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 로그인 - 아이디/비밀번호 검증 후 액세스·리프레시 토큰 발급
     */
    public TokenResponse login(LoginRequest request) {
        MemberDetails details;
        try {
            details = (MemberDetails) memberService.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException e) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        //비밀번호 불일치
        if (!passwordEncoder.matches(request.password(), details.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD);
        }

        return issueTokens(details.getUsername(), details.getRole().name());
    }

    /**
     * 토큰 갱신 - 리프레시 토큰 유효성 및 저장된 값과 일치 여부 확인 후 재발급
     */
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new BusinessException(ResponseCode.INVALID_REFRESH_TOKEN);
        }

        Map<String, Object> claims = jwtTokenProvider.getClaims(refreshToken);
        String username = claims.get("username").toString();

        //Redis에 저장된 리프레시 토큰과 비교
        String stored = refreshTokenService.find(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.REFRESH_TOKEN_EXPIRED));

        if (!stored.equals(refreshToken)) {
            throw new BusinessException(ResponseCode.REFRESH_TOKEN_MISMATCH);
        }

        MemberDetails details = (MemberDetails) memberService.loadUserByUsername(username);
        return issueTokens(username, details.getRole().name());
    }

    /**
     * 로그아웃 - Redis에서 리프레시 토큰 삭제
     */
    public void logout(String username) {
        refreshTokenService.delete(username);
    }

    /**
     * 액세스·리프레시 토큰 발급 및 리프레시 토큰 Redis 저장
     */
    public TokenResponse issueTokens(String username, String role) {
        String sessionId = java.util.UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.issue(accessExpiration, Map.of("username", username, "role", role, "sid", sessionId));
        String refreshToken = jwtTokenProvider.issueRefreshToken(refreshExpiration, username, sessionId);
        refreshTokenService.save(username, refreshToken, sessionId);
        return new TokenResponse(accessToken, refreshToken);
    }

}