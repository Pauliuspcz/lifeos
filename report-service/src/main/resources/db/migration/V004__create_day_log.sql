CREATE TABLE IF NOT EXISTS day_log(
                                      id BIGINT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      date DATE NOT NULL,
                                      mood INT,
                                      sleep_hours INT,
                                      UNIQUE (user_id, date)
);
create sequence day_log_seq increment by 1 start with 1;