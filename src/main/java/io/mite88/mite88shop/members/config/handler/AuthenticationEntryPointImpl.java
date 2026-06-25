package io.mite88.mite88shop.members.config.handler;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.dto.CommonResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증되지 않은 요청 진입 시 401 JSON 응답 반환 - 기본 리다이렉트 대신 API 응답 형식 유지
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(ResponseCode.UNAUTHORIZED_ACCESS.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), CommonResponse.fail(ResponseCode.UNAUTHORIZED_ACCESS));
    }
}
