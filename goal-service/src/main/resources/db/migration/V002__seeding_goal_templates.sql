-- Catalog seed: 4 areas, 12 focuses, 34 goal templates.

INSERT INTO categories (code, name, description, icon_key, color, sort_order) VALUES
                                                                                  ('BODY', 'Body', 'Fitness, strength and movement', 'dumbbell', '#FF7043', 10),
                                                                                  ('MIND', 'Mind', 'Calm, sleep and reflection', 'brain', '#7E57C2', 20),
                                                                                  ('GROWTH', 'Growth', 'Learn, read and create', 'sprout', '#26A69A', 30),
                                                                                  ('LIFE', 'Life', 'Food, people and everyday routines', 'home', '#66BB6A', 40);

INSERT INTO directions (category_id, code, name, description, icon_key, color, sort_order)
SELECT c.id, v.code, v.name, v.description, v.icon_key, c.color, v.sort_order
FROM (VALUES
          ('BODY', 'MOVE_MORE', 'Move more', 'Add more movement to your days', 'footprints', 10),
          ('BODY', 'GET_STRONGER', 'Get stronger', 'Build strength step by step', 'dumbbell', 20),
          ('BODY', 'BUILD_ENDURANCE', 'Build endurance', 'Go further and keep going longer', 'run', 30),
          ('MIND', 'CALM_DOWN', 'Calm down', 'Less stress, more balance', 'leaf', 10),
          ('MIND', 'SLEEP_BETTER', 'Sleep better', 'Fall asleep easier and wake up rested', 'moon', 20),
          ('MIND', 'REFLECT', 'Reflect', 'Look back on your day and ahead', 'pen', 30),
          ('GROWTH', 'READ_MORE', 'Read more', 'Make room for books', 'book', 10),
          ('GROWTH', 'LEARN_A_SKILL', 'Learn a skill', 'Build a skill one session at a time', 'code', 20),
          ('GROWTH', 'BE_CREATIVE', 'Be creative', 'Make something just because', 'sparkles', 30),
          ('LIFE', 'EAT_DRINK_BETTER', 'Eat and drink better', 'Fuel yourself well', 'utensils', 10),
          ('LIFE', 'STAY_CONNECTED', 'Stay connected', 'Keep in touch with people you like', 'users', 20),
          ('LIFE', 'GET_ORGANISED', 'Get organised', 'Less clutter, more control', 'calendar', 30)
     ) AS v(category_code, code, name, description, icon_key, sort_order)
         JOIN categories c ON c.code = v.category_code;

INSERT INTO goal_templates
(code, category_id, direction_id, title, description, tracking_type, default_period,
 default_times_per_period, default_target_amount, default_unit, default_target_time,
 default_duration_days, outdoor, difficulty, icon_key, color, sort_order)
SELECT v.code, d.category_id, d.id, v.title, v.description, v.tracking_type, v.period,
       CAST(v.times AS INT), CAST(v.amount AS NUMERIC(12, 2)), v.unit, CAST(v.target_time AS TIME),
       CAST(v.duration AS INT), v.outdoor, v.difficulty, v.icon_key, d.color, v.sort_order
FROM (VALUES
          ('MOVE_MORE', 'WALK_10K_STEPS', 'Walk 10,000 steps a day', 'Move more through the day', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, TRUE, 'EASY', 'footprints', 10),
          ('MOVE_MORE', 'STRETCH_10_MIN', 'Stretch for 10 minutes a day', 'Keep your body loose and mobile', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'stretch', 20),
          ('MOVE_MORE', 'WALK_100_KM_MONTH', 'Walk 100 km a month', 'Log your walks and watch the total grow', 'AMOUNT', 'MONTHLY', NULL, 100, 'km', NULL, NULL, TRUE, 'MEDIUM', 'map', 30),
          ('GET_STRONGER', 'STRENGTH_2X_WEEK', 'Strength training twice a week', 'Build strength with two sessions', 'CHECK', 'WEEKLY', 2, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'dumbbell', 10),
          ('GET_STRONGER', 'HOME_WORKOUT_10_MIN', 'Do a 10-minute home workout', 'Short and simple, no gym needed', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'activity', 20),
          ('GET_STRONGER', 'PUSHUPS_500_MONTH', 'Do 500 push-ups this month', 'Log your reps and reach the total', 'AMOUNT', 'MONTHLY', NULL, 500, 'reps', NULL, NULL, FALSE, 'HARD', 'trending-up', 30),
          ('BUILD_ENDURANCE', 'RUN_3X_WEEK', 'Run 3 times a week', 'A steady base for endurance', 'CHECK', 'WEEKLY', 3, NULL, NULL, NULL, NULL, TRUE, 'MEDIUM', 'run', 10),
          ('BUILD_ENDURANCE', 'RUN_1000_KM', 'Run 1,000 km by a date', 'Log your distance and track your pace', 'AMOUNT', NULL, NULL, 1000, 'km', NULL, NULL, TRUE, 'HARD', 'route', 20),
          ('BUILD_ENDURANCE', 'MARATHON_TRAINING_12_WEEKS', 'Marathon training: 12 weeks', 'Run 3 times a week for 12 weeks', 'CHECK', 'WEEKLY', 3, NULL, NULL, NULL, 84, TRUE, 'HARD', 'medal', 30),
          ('CALM_DOWN', 'MEDITATE_10_MIN', 'Meditate for 10 minutes a day', 'Take a short break for your mind', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'leaf', 10),
          ('CALM_DOWN', 'OUTSIDE_20_MIN', 'Spend 20 minutes outside a day', 'Fresh air and daylight', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, TRUE, 'EASY', 'sun', 20),
          ('CALM_DOWN', 'COLD_SHOWER_30_DAYS', '30-day cold shower challenge', 'Finish every day for 30 days', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 30, FALSE, 'HARD', 'snowflake', 30),
          ('SLEEP_BETTER', 'BED_BEFORE_23', 'Go to bed before 23:00', 'A consistent bedtime', 'CHECK', 'DAILY', 1, NULL, NULL, '23:00', NULL, FALSE, 'MEDIUM', 'bed', 10),
          ('SLEEP_BETTER', 'SLEEP_8_HOURS', 'Sleep 8 hours', 'Give your body enough rest', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'moon', 20),
          ('SLEEP_BETTER', 'NO_SCREENS_22', 'No screens after 22:00', 'Wind down without a screen', 'CHECK', 'DAILY', 1, NULL, NULL, '22:00', NULL, FALSE, 'MEDIUM', 'phone-off', 30),
          ('REFLECT', 'JOURNAL_DAILY', 'Write in a journal', 'Reflect on your day', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'pen', 10),
          ('REFLECT', 'GRATITUDE_3_THINGS', 'Write down 3 things you are grateful for', 'End the day on a good note', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'heart', 20),
          ('REFLECT', 'WEEKLY_REVIEW_SUNDAY', 'Do a weekly review on Sunday', 'Look back on the week and plan the next', 'CHECK', 'WEEKLY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'calendar', 30),
          ('READ_MORE', 'READ_20_MIN', 'Read for 20 minutes a day', 'A small daily reading habit', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'book', 10),
          ('READ_MORE', 'READ_12_BOOKS', 'Read 12 books by a date', 'Log each finished book and track your pace', 'AMOUNT', NULL, NULL, 12, 'books', NULL, NULL, FALSE, 'MEDIUM', 'library', 20),
          ('LEARN_A_SKILL', 'PROGRAMMING_30_MIN', 'Practice programming for 30 minutes a day', 'Build the skill one session at a time', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'code', 10),
          ('LEARN_A_SKILL', 'LANGUAGE_15_MIN', 'Practice a language for 15 minutes a day', 'Small steps add up to fluency', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'languages', 20),
          ('LEARN_A_SKILL', 'INSTRUMENT_20_MIN', 'Practice an instrument for 20 minutes a day', 'Keep your fingers moving', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'music', 30),
          ('BE_CREATIVE', 'CREATE_15_MIN', 'Draw or write for 15 minutes a day', 'Make a little room for creativity', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'pen-tool', 10),
          ('BE_CREATIVE', 'SIDE_PROJECT_3X_WEEK', 'Work on a side project 3 times a week', 'Keep your own project moving', 'CHECK', 'WEEKLY', 3, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'rocket', 20),
          ('BE_CREATIVE', 'CREATE_30_DAYS', '30 days of creating something', 'Make something every day for 30 days', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 30, FALSE, 'HARD', 'sparkles', 30),
          ('EAT_DRINK_BETTER', 'WATER_3_LITERS', 'Drink 3 liters of water a day', 'Stay hydrated through the day', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'droplet', 10),
          ('EAT_DRINK_BETTER', 'COFFEE_MAX_2', 'Max 2 coffees a day', 'Keep caffeine under control', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'coffee', 20),
          ('EAT_DRINK_BETTER', 'NO_ALCOHOL_SWEETS_30_DAYS', '30 days without alcohol or sweets', 'Go the full 30 days', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 30, FALSE, 'HARD', 'ban', 30),
          ('STAY_CONNECTED', 'FRIEND_WEEKLY', 'Meet a friend once a week', 'Stay in touch in person', 'CHECK', 'WEEKLY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'users', 10),
          ('STAY_CONNECTED', 'CALL_FAMILY_WEEKLY', 'Call family once a week', 'A quick call goes a long way', 'CHECK', 'WEEKLY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'phone', 20),
          ('STAY_CONNECTED', 'SOCIAL_EVENT_MONTHLY', 'Join a social event once a month', 'Get out and meet people', 'CHECK', 'MONTHLY', 1, NULL, NULL, NULL, NULL, FALSE, 'MEDIUM', 'party-popper', 30),
          ('GET_ORGANISED', 'TIDY_10_MIN', 'Tidy for 10 minutes a day', 'A little order every day', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'home', 10),
          ('GET_ORGANISED', 'PLAN_WEEK_SUNDAY', 'Plan your week on Sunday', 'Start the week with a plan', 'CHECK', 'WEEKLY', 1, NULL, NULL, NULL, NULL, FALSE, 'EASY', 'calendar', 20)
     ) AS v(direction_code, code, title, description, tracking_type, period, times, amount, unit,
            target_time, duration, outdoor, difficulty, icon_key, sort_order)
         JOIN directions d ON d.code = v.direction_code;