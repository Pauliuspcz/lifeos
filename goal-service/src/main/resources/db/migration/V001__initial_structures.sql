CREATE TABLE IF NOT EXISTS goals(
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    period VARCHAR(20) NOT NULL,
    times_per_period INT NOT NULL CHECK (times_per_period > 0),
    amount_per_session NUMERIC (10, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
create sequence goal_seq increment by 1 start with 1;