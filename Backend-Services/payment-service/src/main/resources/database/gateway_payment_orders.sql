CREATE TABLE IF NOT EXISTS gateway_payment_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reference VARCHAR(64) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(128) NOT NULL,
    student_id BIGINT NOT NULL,
    monthly_fee_ids VARCHAR(2000) NOT NULL,
    amount_paise BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    qr_image_url VARCHAR(1000),
    qr_payload VARCHAR(4000),
    provider_payment_id VARCHAR(128),
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_gateway_payment_orders_reference (reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
