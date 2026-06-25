package io.mite88.mite88shop.members.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MemberDetails implements UserDetails {

    private final String username;
    private final String password;

    private final Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = this.role.name().toUpperCase();
        return List.of(new SimpleGrantedAuthority(role));
    }


}
