CREATE TABLE posts
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    title      VARCHAR(255)          NULL,
    content    VARCHAR(255)          NULL,
    member_id  BIGINT                NULL,
    created_at TIMESTAMP             NULL,
    updated_at TIMESTAMP             NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);