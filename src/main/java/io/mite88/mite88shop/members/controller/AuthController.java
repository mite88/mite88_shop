package io.mite88.mite88shop.members.controller;

import io.mite88.mite88shop.members.dto.*;
import io.mite88.mite88shop.members.service.AuthService;
import io.mite88.mite88shop.members.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<MemberDescription> signup(@Valid @RequestBody MemberSaveRequest request) {
        return ResponseEntity.ok(memberService.save(request));
    }

    /** 일반 로그인 */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** AccessToken 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal MemberDetails details) {
        authService.logout(details.getUsername());
        return ResponseEntity.noContent().build();
    }

}
