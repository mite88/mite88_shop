package io.mite88.mite88shop.members.config;

import io.mite88.mite88shop.members.config.filter.TokenAuthenticationFilter;
import io.mite88.mite88shop.members.config.handler.AuthenticationEntryPointImpl;
import io.mite88.mite88shop.members.config.handler.OAuth2SuccessHandler;
import io.mite88.mite88shop.members.service.OAuth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final OAuth2MemberService oAuth2MemberService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationEntryPointImpl authenticationEntryPoint; // Inject AuthenticationEntryPointImpl

    /**
     * 보안 필터 체인 설정 - JWT 인증, URL 접근 권한, OAuth2 조건부 등록
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Qualifier("authenticationSuccessHandlerImpl") AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository
    ) throws Exception {
        HttpSecurity security = http
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .formLogin(f -> f
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                //JWT 사용 시 세션 불필요하나, OAuth2 리다이렉트를 위해 IF_REQUIRED 유지
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/", "/login", "/signup").permitAll()
                        //인증 없이 사용 가능한 인증 API
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        //문의 게시판: 조회 공개, 문의 작성 로그인 회원, 수정/삭제/답변 ADMIN 전용
                        .requestMatchers(HttpMethod.GET, "/qna/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/qna").authenticated()
                        .requestMatchers("/qna/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/members").permitAll()
                        .requestMatchers("/members/**").authenticated()
                        .requestMatchers("/cart", "/orders").permitAll()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        //상품 조회는 공개, 등록/수정/삭제는 ADMIN만 가능
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                //UsernamePasswordAuthenticationFilter 앞에 JWT 필터 삽입
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //Google OAuth2 클라이언트 설정이 있을 때만 OAuth2 로그인 등록
        if (clientRegistrationRepository.getIfAvailable() != null) {
            security.oauth2Login(oauth -> oauth
                    .userInfoEndpoint(ui -> ui.userService(oAuth2MemberService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(failureHandler)
            );
        }

        return security.build();
    }
}