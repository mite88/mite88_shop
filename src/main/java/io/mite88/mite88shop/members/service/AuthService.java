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

    public TokenResponse login(LoginRequest request) {
        MemberDetails details;
        try {
            details = (MemberDetails) memberService.loadUserByUsername(request.username());
        } catch (UsernameNotFoundException e) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }


        if (!passwordEncoder.matches(request.password(), details.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD);
        }

        return issueTokens(details.getUsername(), details.getRole().name());
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw new BusinessException(ResponseCode.INVALID_REFRESH_TOKEN);
        }

        Map<String, Object> claims = jwtTokenProvider.getClaims(refreshToken);
        String username = claims.get("username").toString();

        String stored = refreshTokenService.find(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.REFRESH_TOKEN_EXPIRED));

        if (!stored.equals(refreshToken)) {
            throw new BusinessException(ResponseCode.REFRESH_TOKEN_MISMATCH);
        }

        MemberDetails details = (MemberDetails) memberService.loadUserByUsername(username);
        return issueTokens(username, details.getRole().name());
    }

    public void logout(String username) {
        refreshTokenService.delete(username);
    }

    public TokenResponse issueTokens(String username, String role) {
        String accessToken = jwtTokenProvider.issue(accessExpiration, Map.of("username", username, "role", role));
        String refreshToken = jwtTokenProvider.issueRefreshToken(refreshExpiration, username);
        refreshTokenService.save(username, refreshToken);
        return new TokenResponse(accessToken, refreshToken);
    }

}