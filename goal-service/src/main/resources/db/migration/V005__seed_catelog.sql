-- Categories: where a goal belongs
INSERT INTO categories (code, name, description, icon_key, color, sort_order) VALUES
                      ('TRAINING',   'Training',      'Running, strength and everyday movement',  'dumbbell', '#FF7043', 10),
                      ('SLEEP',      'Sleep & rest',  'Better sleep and recovery',                'moon',     '#5C6BC0', 20),
                      ('FOOD_DRINK', 'Food & drink',  'Eat, drink and fuel yourself well',        'utensils', '#66BB6A', 30),
                      ('LEARNING',   'Learning',      'Build new skills and knowledge',           'book',     '#26A69A', 40),
                      ('MIND',       'Mind & calm',   'Stress, focus and reflection',             'brain',    '#AB47BC', 50),
                      ('SOCIAL',     'Social',        'Stay connected with the people you like',  'users',    '#FFCA28', 60);

-- Directions: why the user wants to change something
INSERT INTO directions (code, name, description, icon_key, color, sort_order) VALUES
                      ('MORE_ENERGY',    'More energy',          'Feel less tired and more awake',            'bolt',    '#FFA726', 10),
                      ('GET_FITTER',     'Get in better shape',  'Build fitness and strength step by step',   'heart',   '#EF5350', 20),
                      ('COMPETITION',    'Train for a race',     'Prepare for a race or competition',         'trophy',  '#FFB300', 30),
                      ('SLEEP_BETTER',   'Sleep better',         'Fall asleep easier and wake up rested',     'moon',    '#5C6BC0', 40),
                      ('LEARN_NEW',      'Learn something new',  'Build a skill with a daily routine',        'book',    '#26A69A', 50),
                      ('CHALLENGE',      'Challenge myself',     'Test your limits with a clear challenge',   'flame',   '#FF7043', 60),
                      ('CALM_MIND',      'Calm my mind',         'Reduce stress and get more balance',        'leaf',    '#AB47BC', 70);

-- Templates: concrete goals with default values
INSERT INTO goal_templates
(category_id, title, description, goal_type, tracking_type, default_period, default_times_per_period,
 default_target_amount, default_unit, default_target_time, difficulty, icon_key, color, sort_order) VALUES
                ((SELECT id FROM categories WHERE code = 'TRAINING'), 'Run 3 times a week','A steady base for endurance', 'HABIT', 'COUNT', 'WEEKLY', 3, NULL, NULL, NULL, 'MEDIUM', 'run', '#FF7043', 10),
                ((SELECT id FROM categories WHERE code = 'TRAINING'), 'Walk 10,000 steps a day','Move more through the day', 'HABIT', 'AMOUNT', 'DAILY', 1, 10000, 'steps', NULL, 'EASY', 'footprints', '#FF8A65', 20),
                ((SELECT id FROM categories WHERE code = 'TRAINING'), 'Strength training twice a week','Build strength with two sessions', 'HABIT', 'COUNT', 'WEEKLY', 2, NULL, NULL, NULL, 'MEDIUM', 'dumbbell', '#F4511E', 30),
                ((SELECT id FROM categories WHERE code = 'TRAINING'), 'Stretch for 10 minutes','Keep your body loose and mobile', 'HABIT', 'DURATION', 'DAILY', 1, 10, 'min', NULL, 'EASY', 'stretch', '#FFAB91', 40),
                ((SELECT id FROM categories WHERE code = 'TRAINING'), 'Run a marathon','Pick a date and build up towards it', 'EVENT', 'EVENT', NULL, 1, NULL, NULL, NULL, 'HARD', 'medal', '#FFB300', 50),
                ((SELECT id FROM categories WHERE code = 'SLEEP'), 'Go to bed before 23:00','A consistent bedtime', 'HABIT', 'TIME_TARGET', 'DAILY', 1, NULL, NULL, TIME '23:00', 'MEDIUM', 'bed', '#5C6BC0', 10),
                ((SELECT id FROM categories WHERE code = 'SLEEP'), 'Sleep 8 hours','Give your body enough rest', 'HABIT', 'DURATION', 'DAILY', 1, 8, 'h', NULL, 'MEDIUM', 'moon', '#7986CB', 20),
                ((SELECT id FROM categories WHERE code = 'SLEEP'), 'No screens after 22:00','Wind down without a screen', 'HABIT', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 'MEDIUM', 'phone-off', '#9FA8DA', 30),
                ((SELECT id FROM categories WHERE code = 'FOOD_DRINK'), 'Drink 3 liters of water','Stay hydrated through the day', 'HABIT', 'AMOUNT', 'DAILY', 1, 3, 'L', NULL, 'EASY', 'droplet', '#29B6F6', 10),
                ((SELECT id FROM categories WHERE code = 'FOOD_DRINK'), 'Max 2 coffees a day','Keep caffeine under control', 'HABIT', 'LIMIT', 'DAILY', 1, 2, 'cups', NULL, 'MEDIUM', 'coffee', '#8D6E63', 20),
                ((SELECT id FROM categories WHERE code = 'FOOD_DRINK'), 'Eat 5 portions of fruit and veg','More vitamins on your plate', 'HABIT', 'AMOUNT', 'DAILY', 1, 5, 'portions', NULL, 'MEDIUM', 'apple', '#66BB6A', 30),
                ((SELECT id FROM categories WHERE code = 'LEARNING'), 'Read for 20 minutes','A small daily reading habit', 'HABIT', 'DURATION', 'DAILY', 1, 20, 'min', NULL, 'EASY', 'book', '#26A69A', 10),
                ((SELECT id FROM categories WHERE code = 'LEARNING'), 'Practice programming for 30 minutes','Build the skill one session at a time', 'HABIT', 'DURATION', 'DAILY', 1, 30, 'min', NULL, 'MEDIUM', 'code', '#00897B', 20),
                ((SELECT id FROM categories WHERE code = 'MIND'), 'Meditate for 10 minutes','Take a short break for your mind', 'HABIT', 'DURATION', 'DAILY', 1, 10, 'min', NULL, 'EASY', 'leaf', '#AB47BC', 10),
                ((SELECT id FROM categories WHERE code = 'MIND'), 'Write in a journal','Reflect on your day', 'HABIT', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 'EASY', 'pen', '#BA68C8', 20),
                ((SELECT id FROM categories WHERE code = 'MIND'), '30-day cold shower challenge','Finish every day for 30 days', 'OTHER', 'CHECK', 'DAILY', 1, NULL, NULL, NULL, 'HARD', 'snowflake', '#4FC3F7', 30),
                ((SELECT id FROM categories WHERE code = 'SOCIAL'), 'Meet a friend once a week','Stay in touch in person', 'HABIT', 'COUNT', 'WEEKLY', 1, NULL, NULL, NULL, 'EASY', 'users', '#FFCA28', 10);

-- Which templates are suggested under which direction
INSERT INTO direction_templates (direction_id, template_id)
SELECT d.id, t.id
FROM (VALUES
          ('MORE_ENERGY',  'Drink 3 liters of water'),
          ('MORE_ENERGY',  'Sleep 8 hours'),
          ('MORE_ENERGY',  'Walk 10,000 steps a day'),
          ('MORE_ENERGY',  'Go to bed before 23:00'),
          ('GET_FITTER',   'Run 3 times a week'),
          ('GET_FITTER',   'Walk 10,000 steps a day'),
          ('GET_FITTER',   'Strength training twice a week'),
          ('GET_FITTER',   'Stretch for 10 minutes'),
          ('GET_FITTER',   'Drink 3 liters of water'),
          ('GET_FITTER',   'Eat 5 portions of fruit and veg'),
          ('COMPETITION',  'Run a marathon'),
          ('COMPETITION',  'Run 3 times a week'),
          ('COMPETITION',  'Drink 3 liters of water'),
          ('COMPETITION',  'Sleep 8 hours'),
          ('COMPETITION',  'Stretch for 10 minutes'),
          ('SLEEP_BETTER', 'Go to bed before 23:00'),
          ('SLEEP_BETTER', 'Sleep 8 hours'),
          ('SLEEP_BETTER', 'No screens after 22:00'),
          ('SLEEP_BETTER', 'Max 2 coffees a day'),
          ('LEARN_NEW',    'Read for 20 minutes'),
          ('LEARN_NEW',    'Practice programming for 30 minutes'),
          ('CHALLENGE',    '30-day cold shower challenge'),
          ('CHALLENGE',    'Run a marathon'),
          ('CHALLENGE',    'Strength training twice a week'),
          ('CALM_MIND',    'Meditate for 10 minutes'),
          ('CALM_MIND',    'Write in a journal'),
          ('CALM_MIND',    'No screens after 22:00'),
          ('CALM_MIND',    'Meet a friend once a week')
     ) AS l(direction_code, template_title)
         JOIN directions d ON d.code = l.direction_code
         JOIN goal_templates t ON t.title = l.template_title;