CREATE TABLE ai_job_log
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    job_id    VARCHAR(255)          NOT NULL,
    status    VARCHAR(255)          NOT NULL,
    timestamp TIMESTAMP             NOT NULL,
    message   VARCHAR(500)          NULL,
    CONSTRAINT pk_ai_job_log PRIMARY KEY (id)
);