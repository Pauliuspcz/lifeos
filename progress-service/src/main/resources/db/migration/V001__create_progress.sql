CREATE TABLE IF NOT EXISTS progress(
    id BIGINT PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    amount NUMERIC(10,2) CHECK (amount > 0),
    date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
create sequence progress_seq increment by 1 start with 1;
create index idx_progress_goal ON progress(goal_id);