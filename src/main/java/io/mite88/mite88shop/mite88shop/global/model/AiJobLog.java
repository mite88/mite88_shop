package io.mite88.mite88shop.mite88shop.global.model;

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

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, DONE, FAILED

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String message; // Optional message for the log entry

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
