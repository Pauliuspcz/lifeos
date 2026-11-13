CREATE TABLE outdoor_goal (goal_id BIGINT PRIMARY KEY,user_id BIGINT NOT NULL );

CREATE INDEX idx_outdoor_goal_user ON outdoor_goal (user_id);