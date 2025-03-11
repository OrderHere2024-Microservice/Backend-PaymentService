CREATE TABLE payments
(
    payment_id     serial PRIMARY KEY          NOT NULL UNIQUE,
    order_id       integer                      NOT NULL ,
    payment_status varchar(100)              NOT NULL,
    payment_method varchar(255),
    stripe_payment_id varchar(255)             NOT NULL ,
    amount         decimal(10, 2)              NOT NULL,
    currency       varchar(3)                  NOT NULL,
    created_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);