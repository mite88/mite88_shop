CREATE TABLE product (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       INT          NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    category    VARCHAR(100),
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
