package io.mite88.mite88shop.members.entity;

import io.mite88.mite88shop.members.dto.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    private String email;

    private String providerId; // OAuth2 (e.g. google sub)

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Member(String username, String password, String email, String providerId, Role role) { // role 인자 추가
        this.username = username;
        this.password = password;
        this.email = email;
        this.providerId = providerId;
        this.role = (role != null) ? role : Role.MEMBER; // role이 제공되면 사용, 아니면 기본값 MEMBER
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Member oauthMember(String username, String email) {
        Member member = new Member();
        member.username = username;
        member.password = "";
        member.email = email;
        member.role = Role.MEMBER;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        return member;
    }


}