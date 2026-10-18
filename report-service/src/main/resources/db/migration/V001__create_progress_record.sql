CREATE TABLE IF NOT EXISTS progress_record(
    id BIGINT PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    progress_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    amount NUMERIC(10,2),
    date DATE NOT NULL,
    received_at TIMESTAMPTZ NOT NULL
);
create sequence progress_record_seq increment by 1 start with 1;
create index idx_progress_record_user_goal ON progress_record(user_id, goal_id);