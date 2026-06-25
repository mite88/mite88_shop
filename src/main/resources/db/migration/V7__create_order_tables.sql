CREATE TABLE orders (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    member_id   BIGINT       NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ORDERED',
    total_price INT          NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_item (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    product_id   BIGINT       NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price        INT          NOT NULL,
    quantity     INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
