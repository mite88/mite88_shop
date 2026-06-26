package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberSaveRequest;
import io.mite88.mite88shop.members.dto.Role; // Role import
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberSaveRequest memberSaveRequest;
    private Member member;
    private String username = "testuser";
    private String password = "password123";
    private String email = "test@example.com";
    private String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
        memberSaveRequest = new MemberSaveRequest(username, password, email);
        member = Member.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("회원 저장 성공")
    void save_Success() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberJpaRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(memberJpaRepository.saveAndFlush(any(Member.class))).thenReturn(member);

        MemberDescription result = memberService.save(memberSaveRequest);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.email()).isEqualTo(email);

        verify(memberJpaRepository, times(1)).findByUsername(username);
        verify(memberJpaRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(memberJpaRepository, times(1)).saveAndFlush(any(Member.class));
    }

    @Test
    @DisplayName("회원 저장 실패 - 중복된 사용자 이름")
    void save_DuplicateUsername_ThrowsBusinessException() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.of(member));

        BusinessException exception = assertThrows(BusinessException.class, () -> memberService.save(memberSaveRequest));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.DUPLICATE_USERNAME);
        verify(memberJpaRepository, times(1)).findByUsername(username);
        verify(memberJpaRepository, times(0)).findByEmail(anyString());
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(memberJpaRepository, times(0)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 저장 실패 - 중복된 이메일")
    void save_DuplicateEmail_ThrowsBusinessException() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberJpaRepository.findByEmail(email)).thenReturn(Optional.of(member));

        BusinessException exception = assertThrows(BusinessException.class, () -> memberService.save(memberSaveRequest));

        assertThat(exception.getResponseCode()).isEqualTo(ResponseCode.DUPLICATE_EMAIL);
        verify(memberJpaRepository, times(1)).findByUsername(username);
        verify(memberJpaRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(0)).encode(anyString());
        verify(memberJpaRepository, times(0)).save(any(Member.class));
    }

    @Test
    @DisplayName("사용자 이름으로 사용자 로드 성공")
    void loadUserByUsername_Success() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.of(member));

        UserDetails userDetails = memberService.loadUserByUsername(username);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly(Role.MEMBER.name()); // MemberRole.USER 대신 Role.MEMBER 사용

        verify(memberJpaRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("사용자 이름으로 사용자 로드 실패 - 사용자 없음")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername(username));

        assertThat(exception.getMessage()).contains("User not found with username: " + username);
        verify(memberJpaRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("사용자 이름으로 회원 찾기 성공")
    void findByUsername_Success() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.of(member));

        Member foundMember = memberService.findByUsername(username);

        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getUsername()).isEqualTo(username);
        assertThat(foundMember.getEmail()).isEqualTo(email);

        verify(memberJpaRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("사용자 이름으로 회원 찾기 실패 - 회원 없음")
    void findByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        when(memberJpaRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> memberService.findByUsername(username));

        assertThat(exception.getMessage()).contains("User not found with username: " + username);
        verify(memberJpaRepository, times(1)).findByUsername(username);
    }
}
