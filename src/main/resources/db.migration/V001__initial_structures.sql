CREATE TABLE IF NOT EXISTS users(
    id BIGINT primary key,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(255),
    email VARCHAR(255)
);

create sequence user_seq increment by 1 start with 1;