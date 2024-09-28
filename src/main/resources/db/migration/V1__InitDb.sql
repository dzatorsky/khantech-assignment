CREATE TABLE "user"
(
    id   UUID    NOT NULL,
    name VARCHAR NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

-------------------------------------------------------

CREATE TABLE wallet
(
    id       UUID           NOT NULL,
    user_id  UUID           NOT NULL,
    currency VARCHAR(3)     NOT NULL,
    balance  DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT balance_positive_value CHECK (balance >= 0),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT uix_user_currency UNIQUE (user_id, currency)
);

-------------------------------------------------------

CREATE TABLE transaction
(
    id             UUID           NOT NULL,
    user_id        UUID           NOT NULL,
    wallet_id      UUID           NOT NULL,
    amount         DECIMAL(19, 2) NOT NULL CHECK (amount > 0),
    balance_before DECIMAL(19, 2) NOT NULL,
    balance_after  DECIMAL(19, 2) NOT NULL,
    type           VARCHAR(255)   NOT NULL,
    status         VARCHAR(255)   NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES "wallet" (id)
);