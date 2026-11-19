-- Demo progress for demo@lifeos.no (user 1). Dev data only.
-- Goal ids match goal-service V003: 1 = Run 50 km a month, 2 = Code 4x/week, 3 = Clean 2x/week.
-- The WITH block must be identical in report-service V005 (same ids and event ids).
WITH checks AS (
    SELECT g.goal_id, g.model, g.target_count, 'WEEKLY' AS period,
           CAST(NULL AS NUMERIC) AS amount,
           CAST(date_trunc('week', CURRENT_DATE) AS DATE) - 7 * w + day_offset AS log_date
    FROM (VALUES (2, 'WEEKLY_COUNT', 4, ARRAY[0, 1, 3, 5]),   -- Mon, Tue, Thu, Sat
                 (3, 'WEEKLY_COUNT', 2, ARRAY[2, 6]))         -- Wed, Sun
             AS g (goal_id, model, target_count, days)
             CROSS JOIN generate_series(0, 4) AS w                      -- this week + 4 weeks back
             CROSS JOIN unnest(g.days) AS day_offset
    WHERE NOT (g.goal_id = 3 AND w = 1 AND day_offset = 6)     -- clean: only 1 of 2 last week
      AND NOT (g.goal_id = 2 AND w = 0)                        -- code this week: see code_this_week
),
     code_this_week AS (
         -- Today and up to 3 days before it, but not before this Monday (4/4 from Thursday on)
         SELECT 2 AS goal_id, 'WEEKLY_COUNT' AS model, 4 AS target_count, 'WEEKLY' AS period,
                CAST(NULL AS NUMERIC) AS amount,
                CURRENT_DATE - k AS log_date
         FROM generate_series(0, 3) AS k
         WHERE CURRENT_DATE - k >= CAST(date_trunc('week', CURRENT_DATE) AS DATE)
     ),
     runs AS (
         -- Goal 1 starts CURRENT_DATE - 40 (goal-service V003), so periods roll from there:
         -- previous period = start .. start + 1 month - 1, current period = start + 1 month ..
         SELECT 1 AS goal_id, 'CUMULATIVE_SUM' AS model, 1 AS target_count, 'DAILY' AS period,
                CAST(r.km AS NUMERIC) AS amount,
                CASE r.which
                    WHEN 'prev' THEN CURRENT_DATE - 40 + r.day_offset
                    ELSE CAST(CURRENT_DATE - 40 + INTERVAL '1 month' AS DATE) + r.day_offset
                    END AS log_date
         FROM (VALUES ('prev', 2, 7), ('prev', 6, 8), ('prev', 10, 6), ('prev', 14, 9),
                      ('prev', 18, 7), ('prev', 22, 8), ('prev', 26, 7),   -- 52 km: target hit
                      ('cur', 1, 6), ('cur', 4, 8), ('cur', 7, 6))         -- 20 km: 30 km to go
                  AS r (which, day_offset, km)
     ),
     demo AS (
         SELECT ROW_NUMBER() OVER (ORDER BY log_date, goal_id) AS n, e.*
         FROM (SELECT * FROM checks
               UNION ALL SELECT * FROM code_this_week
               UNION ALL SELECT * FROM runs) AS e
         WHERE log_date < CURRENT_DATE
            OR goal_id = 2                                          -- only code is ticked today
     )
INSERT INTO progress (id, event_id, user_id, goal_id, amount, date, created_at, tracking_model)
SELECT n, CAST(md5('demo-progress-' || n) AS UUID), 1, goal_id, amount,
       log_date, log_date + TIME '20:00', model
FROM demo
ON CONFLICT DO NOTHING;

SELECT setval('progress_seq', (SELECT MAX(id) FROM progress));