package io.mite88.mite88shop.global.controller;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.dto.CommonResponse;
import io.mite88.mite88shop.global.model.AiJob;
import io.mite88.mite88shop.global.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * packageName    : io.mite88.mite88shop.mite88shop.global
 * fileName       : AiJobController
 * author         : Admin
 * date           : 26. 6. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 12.        Admin       최초 생성
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiJobController {

    private final JobService jobService;

    /**
     * AI 작업 제출 - 202 Accepted와 함께 jobId 반환
     */
    @PostMapping("/jobs")
    public ResponseEntity<CommonResponse<Map<String, String>>> submit(
            @RequestBody Map<String, String> body) {

        String input = body.get("input");
        //입력값 필수 검증
        if (input == null || input.isBlank()) {
            return ResponseEntity
                    .status(ResponseCode.INPUT_REQUIRED.getHttpStatus())
                    .body(CommonResponse.fail(ResponseCode.INPUT_REQUIRED));
        }

        String jobId = jobService.submitJob(input);
        return ResponseEntity.accepted()
                .body(CommonResponse.success(Map.of("jobId", jobId)));
    }

    /**
     * AI 작업 상태/결과 조회 - 클라이언트 폴링용
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<CommonResponse<AiJob>> getJob(@PathVariable String jobId) {
        AiJob job = jobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity
                    .status(ResponseCode.JOB_NOT_FOUND.getHttpStatus())
                    .body(CommonResponse.fail(ResponseCode.JOB_NOT_FOUND, ResponseCode.JOB_NOT_FOUND.getMessage() + ": " + jobId));
        }
        return ResponseEntity.ok(CommonResponse.success(job));
    }
}