ALTER TABLE member
    ADD CONSTRAINT uk_member_username UNIQUE (username);

ALTER TABLE member
    ADD CONSTRAINT uk_member_email UNIQUE (email);
