CREATE TABLE "user"
(
    id   UUID    NOT NULL,
    name VARCHAR NOT NULL,
    CONSTRAINT pk_userentity PRIMARY KEY (id)
);

-------------------------------------------------------

CREATE TABLE wallet
(
    id       UUID           NOT NULL,
    user_id  UUID           NOT NULL,
    currency VARCHAR(3)     NOT NULL,
    balance  DECIMAL(19, 2) NOT NULL,
    CONSTRAINT pk_walletentity PRIMARY KEY (id)
);

ALTER TABLE wallet
    ADD CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id);

-------------------------------------------------------

CREATE TABLE transaction
(
    id             UUID           NOT NULL,
    user_id        UUID           NOT NULL,
    wallet_id      UUID           NOT NULL,
    amount         DECIMAL(19, 2) NOT NULL,
    balance_before DECIMAL(19, 2) NOT NULL,
    balance_after  DECIMAL(19, 2) NOT NULL,
    type           VARCHAR(255)   NOT NULL,
    status         VARCHAR(255)   NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_transaction PRIMARY KEY (id)
);

ALTER TABLE transaction
    ADD CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id);

ALTER TABLE transaction
    ADD CONSTRAINT fk_transaction_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallet (id);