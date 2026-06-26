package io.mite88.mite88shop.members.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberSaveRequest;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.mapper.MemberMapper;
import io.mite88.mite88shop.members.repository.MemberJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public MemberDescription save(MemberSaveRequest request) {
        if (repository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
        }
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

        Member saved = repository.save(member);

        return MemberMapper.toDescription(saved);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> memberOptional = repository.findByUsername(username);

        Member member = memberOptional.orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );

        return MemberMapper.toDetails(member);
    }

    public Member findByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username)
        );
    }

}