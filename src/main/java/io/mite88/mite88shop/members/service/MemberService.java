package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberSaveRequest;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.mapper.MemberMapper;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberJpaRepository repository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 가입 - 아이디/이메일 중복 검사 후 비밀번호 암호화하여 저장
     */
    @Transactional
    public MemberDescription save(MemberSaveRequest request) {
        //아이디 중복 검사
        if (repository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
        }
        //이메일 중복 검사
        if (repository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException(ResponseCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .username(request.username())
                .password(encodedPassword)
                .email(request.email())
                .providerId(null)
                .build();

        try {
            Member saved = repository.saveAndFlush(member);
            return MemberMapper.toDescription(saved);
        } catch (DataIntegrityViolationException e) {
            // 동시 가입 요청으로 check-then-act 사이에 race 발생한 경우 원인 필드 재확인
            if (repository.findByUsername(request.username()).isPresent()) {
                throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
            }
            throw new BusinessException(ResponseCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * Spring Security 인증용 - username으로 UserDetails 조회
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> memberOptional = repository.findByUsername(username);

        Member member = memberOptional.orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );

        return MemberMapper.toDetails(member);
    }

    /**
     * 도메인 서비스용 - username으로 Member 엔티티 직접 반환
     */
    public Member findByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );
    }

}