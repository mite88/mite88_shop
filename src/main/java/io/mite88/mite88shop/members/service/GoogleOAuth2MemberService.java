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

    /**
     * Google OAuth2 로그인 처리 - providerId/email로 기존 회원 조회 후 없으면 자동 가입
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        //Google 외 다른 provider는 별도 처리 없이 그대로 반환
        if (!"google".equals(registrationId)) {
            return oauth2User;
        }

        Map<String, Object> attributes = new LinkedHashMap<>(oauth2User.getAttributes());
        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();
        //Google 계정 고유 username: "google_{sub}"
        String username = "google_" + providerId;

        //username → email 순서로 조회, 없으면 신규 가입
        Member member = repository.findByUsername(username)
                .or(() -> repository.findByEmail(email))
                .orElseGet(() -> repository.save(Member.oauthMember(username, email)));

        //인증 후 필터에서 username/role을 꺼내 쓸 수 있도록 attributes에 추가
        attributes.put("username", member.getUsername());
        attributes.put("role", member.getRole().name());

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(Role.MEMBER.name())),
                attributes,
                "username"
        );
    }
}
