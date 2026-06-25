package io.mite88.mite88shop.global.config;

import io.mite88.mite88shop.global.model.AiJob;
import io.mite88.mite88shop.global.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * packageName    : io.mite88.mite88shop.mite88shop.global.config
 * fileName       : AiJobWorker
 * author         : Admin
 * date           : 26. 6. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 12.        Admin       최초 생성
 */
@Component
@Slf4j
@EnableScheduling
public class AiJobWorker {


    private final RedisTemplate<String, String> queueRedisTemplate;

    private final JobService jobService;
    private final AiModelClient aiModelClient;

    public AiJobWorker(
            @Qualifier("queueRedisTemplate") RedisTemplate<String, String> queueRedisTemplate,
            JobService jobService,
            AiModelClient aiModelClient
    ) {
        this.queueRedisTemplate = queueRedisTemplate;
        this.jobService = jobService;
        this.aiModelClient = aiModelClient;
    }

    @Value("${AI_JOB_QUEUE_KEY}")
    private String queueKey;

    @Scheduled(fixedDelayString = "${AI_JOB_WORKER_DELAY}")
    public void processQueue() {
        // LPOP: 큐에서 꺼내기 (없으면 null)
        String jobId = queueRedisTemplate.opsForList().leftPop(queueKey);
        if (jobId == null) return;

        AiJob job = jobService.getJob(jobId);
        if (job == null) {
            log.warn("Job not found: {}", jobId);
            return;
        }

        log.info("Processing job: {}", jobId);

        job = AiJob.builder()
                .jobId(job.jobId())
                .status("PROCESSING")
                .input(job.input())
                .result(job.result())
                .errorMessage(job.errorMessage())
                .createdAt(job.createdAt())
                .updatedAt(LocalDateTime.now())
                .build();

        jobService.updateJob(job);

        try {
            Object result = aiModelClient.callModel(job.input());

            job = AiJob.builder()
                    .jobId(job.jobId())
                    .status("DONE")
                    .input(job.input())
                    .result(result)
                    .errorMessage(null)
                    .createdAt(job.createdAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            log.info("Job completed: {}", jobId);
        } catch (Exception e) {
            log.error("Job failed: {} - {}", jobId, e.getMessage());

            job = AiJob.builder()
                    .jobId(job.jobId())
                    .status("FAILED")
                    .input(job.input())
                    .result(job.result())
                    .errorMessage(e.getMessage())
                    .createdAt(job.createdAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        jobService.updateJob(job);
    }

}