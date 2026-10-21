ALTER TABLE goals DROP CONSTRAINT ck_goals_period;
ALTER TABLE goals ADD CONSTRAINT ck_goals_period
    CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'));