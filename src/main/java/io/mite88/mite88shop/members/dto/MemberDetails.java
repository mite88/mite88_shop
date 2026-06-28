package io.mite88.mite88shop.members.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 인증 정보 및 OAuth2User 역할 겸용 DTO
 * TokenAuthenticationFilter와 OAuth2SuccessHandler에서 principal로 사용됨
 */
@Getter
@RequiredArgsConstructor
public class MemberDetails implements UserDetails {

    private final String username;
    private final String password;

    private final Role role;

    /**
     * 권한 반환 - role을 GrantedAuthority로 변환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + this.role.name().toUpperCase();
        return List.of(new SimpleGrantedAuthority(role));
    }


}
