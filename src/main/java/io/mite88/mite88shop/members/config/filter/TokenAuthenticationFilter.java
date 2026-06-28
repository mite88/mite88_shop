package io.mite88.mite88shop.members.config.filter;


import io.mite88.mite88shop.members.service.JwtTokenProvider;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.members.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 요청마다 Authorization 헤더에서 JWT 추출 후 유효하면 SecurityContext에 인증 정보 설정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if ( token != null && jwtTokenProvider.validate(token) ) {

            Map<String, Object> payload = jwtTokenProvider.getClaims(token);

            String username = payload.get("username").toString();
            Object sidObj = payload.get("sid");

            if (sidObj != null) {
                String sid = sidObj.toString();
                String activeSid = refreshTokenService.getActiveSessionId(username).orElse(null);
                if (activeSid == null || !activeSid.equals(sid)) {
                    // 다중 로그인 혹은 세션 만료로 인해 비활성화된 세션 ID
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                // 이전 토큰 혹은 sid가 없는 토큰 -> 차단
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = memberService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

            //SecurityContext에 인증 정보 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }

        filterChain.doFilter(request, response);

    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if ( bearerToken != null && bearerToken.startsWith("Bearer ") ) {
            //"Bearer " 이후의 실제 토큰 값만 반환
            return bearerToken.substring(7);
        }
        return null;
    }



}
