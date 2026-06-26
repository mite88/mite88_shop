package io.mite88.mite88shop.members.controller;

import io.mite88.mite88shop.members.dto.MemberDescription;
import io.mite88.mite88shop.members.dto.MemberSaveRequest;
import io.mite88.mite88shop.members.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberApiController{

    private final MemberService service;

    @PostMapping("/members")
    public ResponseEntity<MemberDescription> saveMember(
         @RequestBody MemberSaveRequest request
    ){
        MemberDescription memberDescription = service.save(request);
        return ResponseEntity.ok(memberDescription);
    }

}