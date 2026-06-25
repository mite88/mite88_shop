package io.mite88.mite88shop.members.config.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    /**
     * 로그인 실패 시 401 응답 반환
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "인증에 오류가 발생했습니다!"
        );
    }

}
