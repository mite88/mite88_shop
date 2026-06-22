CREATE TABLE member
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    username    VARCHAR(255)          NULL,
    password    VARCHAR(255)          NULL,
    email       VARCHAR(255)          NULL,
    provider_id VARCHAR(255)          NULL,
    `role`      VARCHAR(255)          NULL,
    created_at  TIMESTAMP             NULL,
    updated_at  TIMESTAMP             NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);