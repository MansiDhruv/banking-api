CREATE TABLE account_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_after DECIMAL(19,4) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_account_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
);