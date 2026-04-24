--liquibase formatted sql

--changeset codex:003-demo-data
INSERT INTO tags (id, name, created_at)
VALUES
    ('30000000-0000-0000-0000-000000000001', 'java', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000002', 'spring', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000003', 'postgresql', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000004', 'react', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000005', 'docker', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000006', 'kubernetes', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_directions (id, code, name, created_at, updated_at)
VALUES
    ('12000000-0000-0000-0000-000000000001', 'BACKEND', 'Backend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('12000000-0000-0000-0000-000000000002', 'FRONTEND', 'Frontend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('12000000-0000-0000-0000-000000000003', 'DEVOPS', 'DevOps', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_levels (id, code, name, created_at, updated_at)
VALUES
    ('13000000-0000-0000-0000-000000000001', 'JUNIOR', 'Junior', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('13000000-0000-0000-0000-000000000002', 'MIDDLE', 'Middle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (
    id, role_id, email, password_hash, first_name, last_name, status, email_verified, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000001',
    roles.id,
    'admin@interview-coach.local',
    '$2a$10$.RAo8hVCWJeCbI/DytWG/uIQL0284TtN3qpclfrdnMAD4cl0wKA..',
    'System',
    'Admin',
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM roles
WHERE roles.code = 'ADMIN'
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (
    id, role_id, email, password_hash, first_name, last_name, status, email_verified, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000002',
    roles.id,
    'demo@interview-coach.local',
    '$2a$10$.RAo8hVCWJeCbI/DytWG/uIQL0284TtN3qpclfrdnMAD4cl0wKA..',
    'Demo',
    'User',
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM roles
WHERE roles.code = 'USER'
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_preferences (
    id, user_id, preferred_direction_id, preferred_level_id, preferred_language, interface_language, theme, updated_at
)
VALUES
    ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', '13000000-0000-0000-0000-000000000002', 'ru', 'ru', 'system', CURRENT_TIMESTAMP),
    ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000001', '13000000-0000-0000-0000-000000000002', 'ru', 'ru', 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_profiles (
    id, title, description, direction_id, level_id, status, created_by, published_at, created_at, updated_at
)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        'Backend Java Junior',
        'Базовый сценарий для начинающего backend-разработчика: Java Core, Spring Boot, REST и SQL.',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000001',
        'PUBLISHED',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '20000000-0000-0000-0000-000000000002',
        'Backend Java Middle',
        'Сценарий среднего уровня: транзакции, многопоточность, производительность, индексы и интеграции.',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000002',
        'PUBLISHED',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '20000000-0000-0000-0000-000000000003',
        'DevOps Middle',
        'Сценарий по Docker, Kubernetes, CI/CD и наблюдаемости для middle DevOps инженера.',
        '12000000-0000-0000-0000-000000000003',
        '13000000-0000-0000-0000-000000000002',
        'PUBLISHED',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO questions (
    id, text, question_type, difficulty_id, direction_id, status, created_by, created_at, updated_at
)
VALUES
    ('21000000-0000-0000-0000-000000000001', 'Что такое JVM, JRE и JDK и в чём между ними разница?', 'TECHNICAL', '13000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000002', 'Объясни разницу между @Component, @Service и @Repository в Spring.', 'TECHNICAL', '13000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000003', 'Что такое REST и какие HTTP-методы ты используешь чаще всего?', 'GENERAL', '13000000-0000-0000-0000-000000000001', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000004', 'Как работают транзакции в Spring и зачем нужен propagation?', 'TECHNICAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000005', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 'TECHNICAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000006', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 'BEHAVIORAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000001', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000007', 'Чем отличается Docker image от container и что такое layer cache?', 'TECHNICAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000003', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000008', 'Что такое readinessProbe и livenessProbe в Kubernetes?', 'TECHNICAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000003', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000009', 'Какие метрики и алерты ты бы поставил для backend-сервиса в production?', 'GENERAL', '13000000-0000-0000-0000-000000000002', '12000000-0000-0000-0000-000000000003', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO profile_questions (id, profile_id, question_id, order_index, is_required, created_at)
VALUES
    ('22000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', 0, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000002', 1, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000003', 2, FALSE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000004', 0, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000005', 1, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000006', 2, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000007', 0, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000008', 1, TRUE, CURRENT_TIMESTAMP),
    ('22000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000009', 2, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO profile_tags (id, profile_id, tag_id)
VALUES
    ('23000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),
    ('23000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002'),
    ('23000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003'),
    ('23000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001'),
    ('23000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002'),
    ('23000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003'),
    ('23000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000005'),
    ('23000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000006')
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_sessions (
    id, user_id, profile_id, direction_snapshot_id, level_snapshot_id, state, current_question_index, started_at, finished_at, created_at, updated_at
)
VALUES
    (
        '40000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000002',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '35 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '35 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000003',
        '12000000-0000-0000-0000-000000000003',
        '13000000-0000-0000-0000-000000000002',
        'IN_PROGRESS',
        1,
        CURRENT_TIMESTAMP - INTERVAL '3 hours',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '3 hours',
        CURRENT_TIMESTAMP - INTERVAL '10 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO session_messages (id, session_id, sender_type, message_type, content, sequence_number, created_at)
VALUES
    ('41000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'INTERVIEWER', 'QUESTION', 'Как работают транзакции в Spring и зачем нужен propagation?', 0, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('41000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'USER', 'ANSWER', 'Транзакции задают границы атомарной операции, а propagation управляет тем, как метод встраивается в уже существующую транзакцию.', 1, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '5 minutes'),
    ('41000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000001', 'INTERVIEWER', 'QUESTION', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 2, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '10 minutes'),
    ('41000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001', 'USER', 'ANSWER', 'Индекс помогает на селективных фильтрах и сортировке, но увеличивает стоимость INSERT и UPDATE, если индекс слишком широкий или малополезный.', 3, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '16 minutes'),
    ('41000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000001', 'INTERVIEWER', 'QUESTION', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 4, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '20 minutes'),
    ('41000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000001', 'USER', 'ANSWER', 'Начал бы с метрик, логов и профилирования, затем проверил бы БД, пул соединений, внешние интеграции и горячие точки в коде.', 5, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '27 minutes'),
    ('41000000-0000-0000-0000-000000000007', '40000000-0000-0000-0000-000000000002', 'INTERVIEWER', 'QUESTION', 'Чем отличается Docker image от container и что такое layer cache?', 0, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
    ('41000000-0000-0000-0000-000000000008', '40000000-0000-0000-0000-000000000002', 'USER', 'ANSWER', 'Image это шаблон файловой системы и метаданных, а container это запущенный экземпляр. Layer cache ускоряет повторную сборку.', 1, CURRENT_TIMESTAMP - INTERVAL '2 hours 50 minutes'),
    ('41000000-0000-0000-0000-000000000009', '40000000-0000-0000-0000-000000000002', 'INTERVIEWER', 'QUESTION', 'Что такое readinessProbe и livenessProbe в Kubernetes?', 2, CURRENT_TIMESTAMP - INTERVAL '2 hours 40 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO external_requests (
    id, session_id, request_type, request_status, request_payload, response_payload, attempt_count, sent_at, completed_at, created_at
)
VALUES
    (
        '42000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '30 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '31 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '30 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO session_reports (
    id, session_id, external_request_id, status, summary_text, overall_score, score_source, raw_payload, requested_at, generated_at, created_at, updated_at
)
VALUES
    (
        '43000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        '42000000-0000-0000-0000-000000000001',
        'READY',
        'Кандидат уверенно отвечает по Spring и PostgreSQL, хорошо рассуждает о диагностике проблем и показывает системный подход.',
        84.50,
        'AI',
        '{"summary":"Demo report","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '30 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '31 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '30 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '31 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO report_items (id, report_id, item_type, title, content, score, sort_order, created_at)
VALUES
    ('44000000-0000-0000-0000-000000000001', '43000000-0000-0000-0000-000000000001', 'STRENGTH', 'Spring transactions', 'Кандидат понимает базовые сценарии использования транзакций и propagation.', 88.00, 0, CURRENT_TIMESTAMP),
    ('44000000-0000-0000-0000-000000000002', '43000000-0000-0000-0000-000000000001', 'STRENGTH', 'Диагностика производительности', 'Есть внятный подход через метрики, профилирование и анализ зависимостей.', 86.00, 1, CURRENT_TIMESTAMP),
    ('44000000-0000-0000-0000-000000000003', '43000000-0000-0000-0000-000000000001', 'WEAKNESS', 'Глубина по индексам', 'Ответ по индексам корректный, но без деталей по селективности, планам запросов и видам индексов.', 68.00, 2, CURRENT_TIMESTAMP),
    ('44000000-0000-0000-0000-000000000004', '43000000-0000-0000-0000-000000000001', 'RECOMMENDATION', 'Углубить PostgreSQL', 'Повторить EXPLAIN ANALYZE, типы индексов и типичные антипаттерны медленных запросов.', NULL, 3, CURRENT_TIMESTAMP),
    ('44000000-0000-0000-0000-000000000005', '43000000-0000-0000-0000-000000000001', 'CATEGORY_SCORE', 'Backend middle readiness', 'Оценка готовности к backend middle интервью.', 84.50, 4, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_sessions (
    id, user_id, profile_id, direction_snapshot_id, level_snapshot_id, state, current_question_index, started_at, finished_at, created_at, updated_at
)
VALUES
    (
        '40000000-0000-0000-0000-000000000010',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000001',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '18 days',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '24 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '24 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000011',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000001',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '13 days',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '28 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000012',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000002',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '9 days',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '33 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '33 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000013',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000002',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '37 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '37 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000014',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000003',
        '12000000-0000-0000-0000-000000000003',
        '13000000-0000-0000-0000-000000000002',
        'FINISHED',
        3,
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '29 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '29 minutes'
    ),
    (
        '40000000-0000-0000-0000-000000000015',
        '10000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000002',
        '12000000-0000-0000-0000-000000000001',
        '13000000-0000-0000-0000-000000000002',
        'PROCESSING',
        3,
        CURRENT_TIMESTAMP - INTERVAL '4 hours',
        CURRENT_TIMESTAMP - INTERVAL '4 hours' + INTERVAL '31 minutes',
        CURRENT_TIMESTAMP - INTERVAL '4 hours',
        CURRENT_TIMESTAMP - INTERVAL '15 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO session_messages (id, session_id, sender_type, message_type, content, sequence_number, created_at)
VALUES
    ('41000000-0000-0000-0000-000000000010', '40000000-0000-0000-0000-000000000010', 'INTERVIEWER', 'QUESTION', 'Что такое JVM, JRE и JDK и в чём между ними разница?', 0, CURRENT_TIMESTAMP - INTERVAL '18 days'),
    ('41000000-0000-0000-0000-000000000011', '40000000-0000-0000-0000-000000000010', 'USER', 'ANSWER', 'JDK нужен для разработки, JRE для запуска приложений, а JVM выполняет байткод и управляет памятью.', 1, CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '4 minutes'),
    ('41000000-0000-0000-0000-000000000012', '40000000-0000-0000-0000-000000000010', 'INTERVIEWER', 'QUESTION', 'Объясни разницу между @Component, @Service и @Repository в Spring.', 2, CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '8 minutes'),
    ('41000000-0000-0000-0000-000000000013', '40000000-0000-0000-0000-000000000010', 'USER', 'ANSWER', 'Это стереотипы Spring, они все участвуют в сканировании бинов, но у Repository есть смысл для слоя доступа к данным.', 3, CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '13 minutes'),
    ('41000000-0000-0000-0000-000000000014', '40000000-0000-0000-0000-000000000010', 'INTERVIEWER', 'QUESTION', 'Что такое REST и какие HTTP-методы ты используешь чаще всего?', 4, CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '18 minutes'),
    ('41000000-0000-0000-0000-000000000015', '40000000-0000-0000-0000-000000000010', 'USER', 'ANSWER', 'Чаще всего GET, POST, PUT и DELETE. REST строится вокруг ресурсов и стандартных HTTP-операций.', 5, CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '22 minutes'),

    ('41000000-0000-0000-0000-000000000016', '40000000-0000-0000-0000-000000000011', 'INTERVIEWER', 'QUESTION', 'Что такое JVM, JRE и JDK и в чём между ними разница?', 0, CURRENT_TIMESTAMP - INTERVAL '13 days'),
    ('41000000-0000-0000-0000-000000000017', '40000000-0000-0000-0000-000000000011', 'USER', 'ANSWER', 'JVM исполняет байткод, JRE содержит среду исполнения, а JDK добавляет компилятор и инструменты разработки.', 1, CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '5 minutes'),
    ('41000000-0000-0000-0000-000000000018', '40000000-0000-0000-0000-000000000011', 'INTERVIEWER', 'QUESTION', 'Объясни разницу между @Component, @Service и @Repository в Spring.', 2, CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '10 minutes'),
    ('41000000-0000-0000-0000-000000000019', '40000000-0000-0000-0000-000000000011', 'USER', 'ANSWER', 'Разница в семантике слоя. Repository ещё может участвовать в преобразовании исключений при работе с БД.', 3, CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '16 minutes'),
    ('41000000-0000-0000-0000-000000000020', '40000000-0000-0000-0000-000000000011', 'INTERVIEWER', 'QUESTION', 'Что такое REST и какие HTTP-методы ты используешь чаще всего?', 4, CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '20 minutes'),
    ('41000000-0000-0000-0000-000000000021', '40000000-0000-0000-0000-000000000011', 'USER', 'ANSWER', 'REST помогает строить API вокруг ресурсов. Я бы использовал GET, POST, PATCH и DELETE в зависимости от задачи.', 5, CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '27 minutes'),

    ('41000000-0000-0000-0000-000000000022', '40000000-0000-0000-0000-000000000012', 'INTERVIEWER', 'QUESTION', 'Как работают транзакции в Spring и зачем нужен propagation?', 0, CURRENT_TIMESTAMP - INTERVAL '9 days'),
    ('41000000-0000-0000-0000-000000000023', '40000000-0000-0000-0000-000000000012', 'USER', 'ANSWER', 'Propagation определяет, создавать ли новую транзакцию или подключаться к существующей. Это важно при вложенных сервисных вызовах.', 1, CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '6 minutes'),
    ('41000000-0000-0000-0000-000000000024', '40000000-0000-0000-0000-000000000012', 'INTERVIEWER', 'QUESTION', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 2, CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '11 minutes'),
    ('41000000-0000-0000-0000-000000000025', '40000000-0000-0000-0000-000000000012', 'USER', 'ANSWER', 'Индекс полезен на селективных запросах и сортировках, но увеличивает стоимость вставки и обновления данных.', 3, CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '18 minutes'),
    ('41000000-0000-0000-0000-000000000026', '40000000-0000-0000-0000-000000000012', 'INTERVIEWER', 'QUESTION', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 4, CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '24 minutes'),
    ('41000000-0000-0000-0000-000000000027', '40000000-0000-0000-0000-000000000012', 'USER', 'ANSWER', 'Сначала посмотрел бы на метрики, затем на slow queries, профилирование, pool соединений и внешние зависимости.', 5, CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '31 minutes'),

    ('41000000-0000-0000-0000-000000000028', '40000000-0000-0000-0000-000000000013', 'INTERVIEWER', 'QUESTION', 'Как работают транзакции в Spring и зачем нужен propagation?', 0, CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('41000000-0000-0000-0000-000000000029', '40000000-0000-0000-0000-000000000013', 'USER', 'ANSWER', 'Propagation влияет на границы и повторное использование транзакций. Например, REQUIRES_NEW отделяет внутреннюю операцию от внешней.', 1, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '7 minutes'),
    ('41000000-0000-0000-0000-000000000030', '40000000-0000-0000-0000-000000000013', 'INTERVIEWER', 'QUESTION', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 2, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '13 minutes'),
    ('41000000-0000-0000-0000-000000000031', '40000000-0000-0000-0000-000000000013', 'USER', 'ANSWER', 'Важно смотреть на селективность, планы выполнения и тип нагрузки. Слишком много индексов вредят write-heavy таблицам.', 3, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '21 minutes'),
    ('41000000-0000-0000-0000-000000000032', '40000000-0000-0000-0000-000000000013', 'INTERVIEWER', 'QUESTION', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 4, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '26 minutes'),
    ('41000000-0000-0000-0000-000000000033', '40000000-0000-0000-0000-000000000013', 'USER', 'ANSWER', 'Сравнил бы baseline и текущие метрики, посмотрел трассировки, ошибки, saturation ресурсов и влияние БД или очередей.', 5, CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '35 minutes'),

    ('41000000-0000-0000-0000-000000000034', '40000000-0000-0000-0000-000000000014', 'INTERVIEWER', 'QUESTION', 'Чем отличается Docker image от container и что такое layer cache?', 0, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('41000000-0000-0000-0000-000000000035', '40000000-0000-0000-0000-000000000014', 'USER', 'ANSWER', 'Image это шаблон, container это исполняемый экземпляр. Layer cache ускоряет rebuild, если ранние слои не менялись.', 1, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '4 minutes'),
    ('41000000-0000-0000-0000-000000000036', '40000000-0000-0000-0000-000000000014', 'INTERVIEWER', 'QUESTION', 'Что такое readinessProbe и livenessProbe в Kubernetes?', 2, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '9 minutes'),
    ('41000000-0000-0000-0000-000000000037', '40000000-0000-0000-0000-000000000014', 'USER', 'ANSWER', 'Liveness показывает, надо ли перезапустить контейнер, readiness отвечает за готовность принимать трафик.', 3, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '15 minutes'),
    ('41000000-0000-0000-0000-000000000038', '40000000-0000-0000-0000-000000000014', 'INTERVIEWER', 'QUESTION', 'Какие метрики и алерты ты бы поставил для backend-сервиса в production?', 4, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '20 minutes'),
    ('41000000-0000-0000-0000-000000000039', '40000000-0000-0000-0000-000000000014', 'USER', 'ANSWER', 'Ошибки, latency, saturation, доступность внешних сервисов, состояние очередей, CPU, память и бизнес-метрики.', 5, CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '27 minutes'),

    ('41000000-0000-0000-0000-000000000040', '40000000-0000-0000-0000-000000000015', 'INTERVIEWER', 'QUESTION', 'Как работают транзакции в Spring и зачем нужен propagation?', 0, CURRENT_TIMESTAMP - INTERVAL '4 hours'),
    ('41000000-0000-0000-0000-000000000041', '40000000-0000-0000-0000-000000000015', 'USER', 'ANSWER', 'Транзакции определяют единицу работы, а propagation задаёт поведение при вызове одного транзакционного метода из другого.', 1, CURRENT_TIMESTAMP - INTERVAL '3 hours 52 minutes'),
    ('41000000-0000-0000-0000-000000000042', '40000000-0000-0000-0000-000000000015', 'INTERVIEWER', 'QUESTION', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 2, CURRENT_TIMESTAMP - INTERVAL '3 hours 45 minutes'),
    ('41000000-0000-0000-0000-000000000043', '40000000-0000-0000-0000-000000000015', 'USER', 'ANSWER', 'Когда есть частые выборки по подходящим колонкам индекс помогает, но на активной записи и плохой селективности может мешать.', 3, CURRENT_TIMESTAMP - INTERVAL '3 hours 37 minutes'),
    ('41000000-0000-0000-0000-000000000044', '40000000-0000-0000-0000-000000000015', 'INTERVIEWER', 'QUESTION', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 4, CURRENT_TIMESTAMP - INTERVAL '3 hours 30 minutes'),
    ('41000000-0000-0000-0000-000000000045', '40000000-0000-0000-0000-000000000015', 'USER', 'ANSWER', 'Проверил бы тайминги, внешние вызовы, БД, логи ошибок и профиль CPU перед тем как делать выводы.', 5, CURRENT_TIMESTAMP - INTERVAL '3 hours 21 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO external_requests (
    id, session_id, request_type, request_status, request_payload, response_payload, attempt_count, sent_at, completed_at, created_at
)
VALUES
    (
        '42000000-0000-0000-0000-000000000010',
        '40000000-0000-0000-0000-000000000010',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '23 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '24 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '23 minutes'
    ),
    (
        '42000000-0000-0000-0000-000000000011',
        '40000000-0000-0000-0000-000000000011',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '27 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '27 minutes'
    ),
    (
        '42000000-0000-0000-0000-000000000012',
        '40000000-0000-0000-0000-000000000012',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '32 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '33 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '32 minutes'
    ),
    (
        '42000000-0000-0000-0000-000000000013',
        '40000000-0000-0000-0000-000000000013',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '36 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '37 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '36 minutes'
    ),
    (
        '42000000-0000-0000-0000-000000000014',
        '40000000-0000-0000-0000-000000000014',
        'FINAL_REPORT',
        'SUCCESS',
        '{"mode":"demo-seed","source":"internal-generator"}'::jsonb,
        '{"status":"READY","generator":"seed"}'::jsonb,
        1,
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '29 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '28 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO session_reports (
    id, session_id, external_request_id, status, summary_text, overall_score, score_source, raw_payload, requested_at, generated_at, created_at, updated_at
)
VALUES
    (
        '43000000-0000-0000-0000-000000000010',
        '40000000-0000-0000-0000-000000000010',
        '42000000-0000-0000-0000-000000000010',
        'READY',
        'Стартовая попытка: уверенная база по Java и Spring, но ответы ещё краткие и без глубины.',
        58.00,
        'FALLBACK',
        '{"summary":"Admin demo report 1","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '23 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '24 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '23 minutes',
        CURRENT_TIMESTAMP - INTERVAL '18 days' + INTERVAL '24 minutes'
    ),
    (
        '43000000-0000-0000-0000-000000000011',
        '40000000-0000-0000-0000-000000000011',
        '42000000-0000-0000-0000-000000000011',
        'READY',
        'Второй заход показывает лучший уровень структуры ответов и более уверенное понимание Spring.',
        66.00,
        'AI',
        '{"summary":"Admin demo report 2","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '27 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '27 minutes',
        CURRENT_TIMESTAMP - INTERVAL '13 days' + INTERVAL '28 minutes'
    ),
    (
        '43000000-0000-0000-0000-000000000012',
        '40000000-0000-0000-0000-000000000012',
        '42000000-0000-0000-0000-000000000012',
        'READY',
        'Кандидат стабильно отвечает по backend middle-темам и лучше аргументирует технические решения.',
        74.00,
        'AI',
        '{"summary":"Admin demo report 3","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '32 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '33 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '32 minutes',
        CURRENT_TIMESTAMP - INTERVAL '9 days' + INTERVAL '33 minutes'
    ),
    (
        '43000000-0000-0000-0000-000000000013',
        '40000000-0000-0000-0000-000000000013',
        '42000000-0000-0000-0000-000000000013',
        'READY',
        'Хорошая зрелость по performance и PostgreSQL, ответы стали более системными и прикладными.',
        82.00,
        'AI',
        '{"summary":"Admin demo report 4","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '36 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '37 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '36 minutes',
        CURRENT_TIMESTAMP - INTERVAL '5 days' + INTERVAL '37 minutes'
    ),
    (
        '43000000-0000-0000-0000-000000000014',
        '40000000-0000-0000-0000-000000000014',
        '42000000-0000-0000-0000-000000000014',
        'READY',
        'По DevOps-направлению ответы уверенные, но глубины по наблюдаемости и SLO пока не хватает.',
        69.00,
        'FALLBACK',
        '{"summary":"Admin demo report 5","source":"seed"}'::jsonb,
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '29 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '28 minutes',
        CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '29 minutes'
    ),
    (
        '43000000-0000-0000-0000-000000000015',
        '40000000-0000-0000-0000-000000000015',
        NULL,
        'PENDING',
        NULL,
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '15 minutes',
        NULL,
        CURRENT_TIMESTAMP - INTERVAL '15 minutes',
        CURRENT_TIMESTAMP - INTERVAL '15 minutes'
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO report_items (id, report_id, item_type, title, content, score, sort_order, created_at)
VALUES
    ('44000000-0000-0000-0000-000000000010', '43000000-0000-0000-0000-000000000010', 'STRENGTH', 'Java fundamentals', 'Есть понимание ролей JVM, JRE и JDK и базовых принципов работы платформы.', 61.00, 0, CURRENT_TIMESTAMP - INTERVAL '18 days'),
    ('44000000-0000-0000-0000-000000000011', '43000000-0000-0000-0000-000000000010', 'WEAKNESS', 'Depth of answers', 'Ответы корректные, но пока короткие и без примеров из практики.', 49.00, 1, CURRENT_TIMESTAMP - INTERVAL '18 days'),
    ('44000000-0000-0000-0000-000000000012', '43000000-0000-0000-0000-000000000010', 'CATEGORY_SCORE', 'Backend junior readiness', 'Первая диагностическая оценка по junior backend профилю.', 58.00, 2, CURRENT_TIMESTAMP - INTERVAL '18 days'),

    ('44000000-0000-0000-0000-000000000013', '43000000-0000-0000-0000-000000000011', 'STRENGTH', 'Spring basics', 'Лучше чувствуется разделение ролей компонентов и слоёв приложения.', 70.00, 0, CURRENT_TIMESTAMP - INTERVAL '13 days'),
    ('44000000-0000-0000-0000-000000000014', '43000000-0000-0000-0000-000000000011', 'RECOMMENDATION', 'Add concrete examples', 'Полезно подкреплять ответы короткими примерами из реальных сервисов или pet-проектов.', NULL, 1, CURRENT_TIMESTAMP - INTERVAL '13 days'),
    ('44000000-0000-0000-0000-000000000015', '43000000-0000-0000-0000-000000000011', 'CATEGORY_SCORE', 'Backend junior readiness', 'Динамика положительная, но до уверенного уровня ещё нужен практический опыт.', 66.00, 2, CURRENT_TIMESTAMP - INTERVAL '13 days'),

    ('44000000-0000-0000-0000-000000000016', '43000000-0000-0000-0000-000000000012', 'STRENGTH', 'Transactions', 'Есть рабочее понимание propagation и типовых сервисных сценариев.', 76.00, 0, CURRENT_TIMESTAMP - INTERVAL '9 days'),
    ('44000000-0000-0000-0000-000000000017', '43000000-0000-0000-0000-000000000012', 'STRENGTH', 'Performance diagnostics', 'Кандидат мыслит через метрики, БД и инфраструктурные ограничения.', 75.00, 1, CURRENT_TIMESTAMP - INTERVAL '9 days'),
    ('44000000-0000-0000-0000-000000000018', '43000000-0000-0000-0000-000000000012', 'CATEGORY_SCORE', 'Backend middle readiness', 'Уровень ближе к уверенному junior+/middle-.', 74.00, 2, CURRENT_TIMESTAMP - INTERVAL '9 days'),

    ('44000000-0000-0000-0000-000000000019', '43000000-0000-0000-0000-000000000013', 'STRENGTH', 'PostgreSQL and indexing', 'Ответы стали глубже и лучше учитывают реальные trade-off в write-heavy системах.', 83.00, 0, CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('44000000-0000-0000-0000-000000000020', '43000000-0000-0000-0000-000000000013', 'STRENGTH', 'System thinking', 'Есть хороший разбор производительности через saturation, tracing и базовую observability.', 81.00, 1, CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('44000000-0000-0000-0000-000000000021', '43000000-0000-0000-0000-000000000013', 'CATEGORY_SCORE', 'Backend middle readiness', 'Сильная попытка, пригодная для наглядного роста на дашборде.', 82.00, 2, CURRENT_TIMESTAMP - INTERVAL '5 days'),

    ('44000000-0000-0000-0000-000000000022', '43000000-0000-0000-0000-000000000014', 'STRENGTH', 'Container fundamentals', 'Хорошо понимается различие image/container и базовый смысл cache layers.', 72.00, 0, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('44000000-0000-0000-0000-000000000023', '43000000-0000-0000-0000-000000000014', 'WEAKNESS', 'Observability depth', 'Не хватает детализации по SLI/SLO, алертам шумоподавления и бизнес-критичным метрикам.', 63.00, 1, CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('44000000-0000-0000-0000-000000000024', '43000000-0000-0000-0000-000000000014', 'CATEGORY_SCORE', 'DevOps middle readiness', 'Уверенная база по инфраструктуре, но есть пространство для роста.', 69.00, 2, CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;
