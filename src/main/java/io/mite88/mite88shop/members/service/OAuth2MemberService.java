package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.mapper.MemberMapper;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2MemberService extends DefaultOAuth2UserService {

    private final MemberJpaRepository repository;

    /**
     * OAuth2 로그인 처리 - providerId로 기존 회원 조회, 없으면 자동 가입 후 MemberDetails 반환
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = attributes.get("sub").toString();
        String email = attributes.get("email").toString();
        String name = attributes.getOrDefault("name", email).toString();

        //providerId로 회원 조회, 없으면 신규 가입
        Member member = repository.findByProviderId(providerId)
                .orElseGet(() -> repository.save(
                        Member.builder()
                                .username(name)
                                .email(email)
                                .providerId(providerId)
                                .build()
                ));

        //MemberDetails는 UserDetails와 OAuth2User를 모두 구현
        return (OAuth2User) MemberMapper.toDetails(member);
    }

}
