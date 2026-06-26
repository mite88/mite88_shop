package io.mite88.mite88shop.members.config.handler;

import io.mite88.mite88shop.members.dto.MemberDetails;
import io.mite88.mite88shop.members.dto.TokenResponse;
import io.mite88.mite88shop.members.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        MemberDetails details = (MemberDetails) authentication.getPrincipal();
        TokenResponse tokens = authService.issueTokens(details.getUsername(), details.getRole().name());

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"accessToken\":\"" + tokens.accessToken() + "\",\"refreshToken\":\"" + tokens.refreshToken() + "\"}"
        );
    }

}
