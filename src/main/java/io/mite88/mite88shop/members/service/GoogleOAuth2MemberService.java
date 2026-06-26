package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.members.dto.Role;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberJpaRepository repository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equals(registrationId)) {
            return oauth2User;
        }

        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());
        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();
        String username = "google_" + providerId;

        Member member = repository.findByUsername(username)
                .or(() -> repository.findByEmail(email))
                .orElseGet(() -> repository.save(Member.oauthMember(username, email)));

        attributes.put("username", member.getUsername());
        attributes.put("role", member.getRole().name());

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(Role.MEMBER.name())),
                attributes,
                "username"
        );
    }
}
