package io.mite88.mite88shop.global.service;

import io.mite88.mite88shop.global.model.AiJob;
import io.mite88.mite88shop.global.model.AiJobLog;
import io.mite88.mite88shop.global.repository.AiJobLogRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * packageName    : io.mite88.mite88shop.mite88shop.global.config.service
 * fileName       : JobService
 * author         : Admin
 * date           : 26. 6. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 12.        Admin       최초 생성
 */
@Service
public class JobService {

    // 데이터 저장용 템플릿 (AiJob 전용)
    private final RedisTemplate<String, AiJob> redisTemplate;

    // 큐 관리용 템플릿 (String 전용 - 에러 해결의 핵심)
    private final RedisTemplate<String, String> queueRedisTemplate;

    private final AiJobLogRepository aiJobLogRepository; // AiJobLogRepository 주입

    public JobService(
            @Qualifier("redisTemplate") RedisTemplate<String, AiJob> redisTemplate,
            @Qualifier("queueRedisTemplate") RedisTemplate<String, String> queueRedisTemplate,
            AiJobLogRepository aiJobLogRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.queueRedisTemplate = queueRedisTemplate;
        this.aiJobLogRepository = aiJobLogRepository;
    }

    @Value("${AI_JOB_QUEUE_KEY}")
    private String queueKey;
    @Value("${AI_JOB_QUEUE_PREFIX}")
    private String jobPrefix;
    @Value("${AI_JOB_QUEUE_TTL}")
    private long ttl;

    @Transactional
    public String submitJob(String input) {
        String jobId = UUID.randomUUID().toString();

        AiJob job = AiJob.builder()
                .jobId(jobId)
                .status("PENDING")
                .input(input)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 1. 데이터 저장 (AiJob 객체 그대로 저장)
        redisTemplate.opsForValue().set(jobPrefix + jobId, job, ttl, TimeUnit.SECONDS);

        // 2. 큐에 jobId 삽입 (String 타입으로 저장)
        queueRedisTemplate.opsForList().rightPush(queueKey, jobId);

        // 3. 로그 기록
        saveJobLog(jobId, "PENDING", "Job submitted");

        return jobId;
    }

    public AiJob getJob(String jobId) {
        return redisTemplate.opsForValue().get(jobPrefix + jobId);
    }

    @Transactional
    public void updateJob(AiJob job) {
        // record는 불변이므로 빌더로 새로 생성하여 덮어쓰기
        AiJob updatedJob = AiJob.builder()
                .jobId(job.jobId())
                .status(job.status())
                .input(job.input())
                .result(job.result())
                .errorMessage(job.errorMessage())
                .createdAt(job.createdAt())
                .updatedAt(LocalDateTime.now())
                .build();

        redisTemplate.opsForValue().set(jobPrefix + job.jobId(), updatedJob, ttl, TimeUnit.SECONDS);

        // 로그 기록
        saveJobLog(updatedJob.jobId(), updatedJob.status(), updatedJob.errorMessage());
    }

    @Transactional
    private void saveJobLog(String jobId, String status, String message) {
        AiJobLog log = AiJobLog.builder()
                .jobId(jobId)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now()) // @PrePersist로 자동 설정되지만 명시적으로 설정
                .build();
        aiJobLogRepository.save(log);
    }
}