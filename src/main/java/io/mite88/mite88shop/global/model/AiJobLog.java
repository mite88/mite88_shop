package io.mite88.mite88shop.global.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_job_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobId;

    //PENDING, PROCESSING, DONE, FAILED 중 하나
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    //에러 메시지 또는 진행 상태 메시지 (선택)
    @Column(length = 500)
    private String message;

    /**
     * 저장 시 현재 시각으로 timestamp 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
