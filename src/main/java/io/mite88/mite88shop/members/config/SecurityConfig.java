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
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)) // Use the injected AuthenticationEntryPointImpl
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/", "/login", "/signup").permitAll()
                        .requestMatchers("/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        .requestMatchers("/posts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/members").permitAll()
                        .requestMatchers("/members/**").authenticated()
                        .requestMatchers("/cart", "/orders").permitAll()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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