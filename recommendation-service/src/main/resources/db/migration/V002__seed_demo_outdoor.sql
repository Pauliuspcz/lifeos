-- Goal 1 (Run 50 km a month) is outdoor; seeded goals don't publish goal.outdoor.changed.
INSERT INTO outdoor_goal (goal_id, user_id) VALUES (1, 1) ON CONFLICT DO NOTHING;