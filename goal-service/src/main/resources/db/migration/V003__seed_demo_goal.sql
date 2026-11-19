-- Demo goals for demo@lifeos.no. Dev data only.
-- Custom goals (no template), like "Create your own goal" in the wizard.
-- Demo ids (must match the progress/report/recommendation seeds):
--   user 1 = demo@lifeos.no (first user in user_db)
--   goal 1 = Run 50 km a month    (BODY, AMOUNT monthly, outdoor)
--   goal 2 = Code 4 times a week  (GROWTH, CHECK weekly)
--   goal 3 = Clean 2 times a week (LIFE, CHECK weekly)
INSERT INTO goals (id, user_id, title, tracking_type, period, times_per_period,
                   target_amount, unit, start_date, outdoor, status,
                   category_id, icon_key, color, created_at, updated_at)
SELECT v.id, 1, v.title, v.tracking_type, v.period, v.times,
       v.amount, v.unit, v.start_date, v.outdoor, 'ACTIVE',
       c.id, v.icon_key, c.color, v.start_date, v.start_date
FROM (VALUES
          (1, 'BODY',   'Run 50 km a month',    'AMOUNT', 'MONTHLY', NULL, 50,   'km', CURRENT_DATE - 40, TRUE,  'run'),
          (2, 'GROWTH', 'Code 4 times a week',  'CHECK',  'WEEKLY',  4,    NULL, NULL, CURRENT_DATE - 35, FALSE, 'code'),
          (3, 'LIFE',   'Clean 2 times a week', 'CHECK',  'WEEKLY',  2,    NULL, NULL, CURRENT_DATE - 35, FALSE, 'home')
     ) AS v (id, category_code, title, tracking_type, period, times, amount, unit, start_date, outdoor, icon_key)
         JOIN categories c ON c.code = v.category_code
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('goals', 'id'), (SELECT MAX(id) FROM goals));