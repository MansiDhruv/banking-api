CREATE TABLE transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reference VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,

    CONSTRAINT fk_transfers_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_transfers_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id)
);