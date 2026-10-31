ALTER TABLE progress ADD COLUMN tracking_model VARCHAR(20);
UPDATE progress SET tracking_model = 'DAILY_CHECK';
ALTER TABLE progress ALTER COLUMN tracking_model SET NOT NULL;

DELETE FROM progress p USING progress d
WHERE p.user_id = d.user_id AND p.goal_id = d.goal_id
  AND p.date = d.date AND p.id > d.id;

CREATE UNIQUE INDEX uq_progress_one_per_day
    ON progress (user_id, goal_id, date)
    WHERE tracking_model <> 'CUMULATIVE_SUM';

CREATE INDEX idx_progress_user_date ON progress (user_id, date);