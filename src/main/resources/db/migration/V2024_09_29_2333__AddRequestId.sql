alter table transaction
    add request_id uuid not null default gen_random_uuid()
        constraint transaction_request_id unique;