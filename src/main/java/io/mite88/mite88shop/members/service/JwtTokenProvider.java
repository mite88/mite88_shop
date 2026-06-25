package io.mite88.mite88shop.members.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class JwtTokenProvider {

    private final String appKey;

    public JwtTokenProvider(
            @Value("${custom.jwt.secrets.app-key}") String appKey
    ) {
        this.appKey = appKey;
    }

    /**
     * JWT 액세스 토큰 발급 - 만료 시간과 클레임 설정 후 서명
     */
    public String issue(long validateTime, Map<String, Object> claims) {

        JwtBuilder jwtBuilder = Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + validateTime))
                .signWith(getSecretKey());

        claims.forEach(jwtBuilder::claim);

        return jwtBuilder.compact();

    }

    /**
     * 리프레시 토큰 발급 - type 클레임으로 refresh 토큰임을 구분
     */
    public String issueRefreshToken(long validateTime, String username) {
        return issue(validateTime, Map.of("username", username, "type", "refresh"));
    }

    /**
     * 토큰 유효성 검사 - 파싱 성공 여부로 판단
     */
    public boolean validate(String token) {

        try {
            getClaims(token);
            return true;
        } catch ( JwtException e ) {
            log.info("잘못된 토큰이 검출되었습니다! 토큰 : {}, 오류 메세지 : {}", token, e.getMessage());
        } catch (IllegalStateException ie ) {
            log.info("토큰이 없거나 토큰에 문제가 있습니다! 토큰 : {}, 오류 메세지 : {}", token, ie.getMessage());
        } catch ( Exception ex ) {
            log.info("토큰 유효성 검사 중 문제가 발생했습니다. 토큰 : {}, 오류 메세지 : {}", token, ex.getMessage());
        }

        return false;
    }

    /**
     * 토큰 페이로드(클레임) 추출
     */
    public Map<String, Object> getClaims(String token) {

        JwtParser parser = Jwts.parser()
                .verifyWith(getSecretKey())
                .build();

        Jws<Claims> result = parser.parseSignedClaims(token);
        return result.getPayload();

    }

    /**
     * HMAC-SHA 서명 키 생성
     */
    private @NonNull SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(appKey.getBytes());
    }


}
