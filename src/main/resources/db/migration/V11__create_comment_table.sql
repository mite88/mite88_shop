CREATE TABLE comment
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    post_id    BIGINT                NOT NULL,
    member_id  BIGINT                NOT NULL,
    content    TEXT                  NOT NULL,
    created_at TIMESTAMP             NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE;

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);
