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

    //OAuth2 제공자 식별자 (예: Google sub)
    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Member(String username, String password, String email, String providerId, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.providerId = providerId;
        //role 미지정 시 기본값 MEMBER
        this.role = (role != null) ? role : Role.MEMBER;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * OAuth2 회원 생성 - 비밀번호 없이 소셜 로그인 전용 회원으로 등록
     */
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