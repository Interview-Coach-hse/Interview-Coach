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
    id, user_id, preferred_direction, preferred_level, preferred_language, interface_language, theme, updated_at
)
VALUES
    ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'BACKEND', 'MIDDLE', 'ru', 'ru', 'system', CURRENT_TIMESTAMP),
    ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'BACKEND', 'MIDDLE', 'ru', 'ru', 'system', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO interview_profiles (
    id, title, description, direction, level, status, created_by, published_at, created_at, updated_at
)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        'Backend Java Junior',
        'Базовый сценарий для начинающего backend-разработчика: Java Core, Spring Boot, REST и SQL.',
        'BACKEND',
        'JUNIOR',
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
        'BACKEND',
        'MIDDLE',
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
        'DEVOPS',
        'MIDDLE',
        'PUBLISHED',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO questions (
    id, text, question_type, difficulty, direction, status, created_by, created_at, updated_at
)
VALUES
    ('21000000-0000-0000-0000-000000000001', 'Что такое JVM, JRE и JDK и в чём между ними разница?', 'TECHNICAL', 'JUNIOR', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000002', 'Объясни разницу между @Component, @Service и @Repository в Spring.', 'TECHNICAL', 'JUNIOR', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000003', 'Что такое REST и какие HTTP-методы ты используешь чаще всего?', 'GENERAL', 'JUNIOR', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000004', 'Как работают транзакции в Spring и зачем нужен propagation?', 'TECHNICAL', 'MIDDLE', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000005', 'Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?', 'TECHNICAL', 'MIDDLE', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000006', 'Как бы ты искал причину деградации производительности у REST API под нагрузкой?', 'BEHAVIORAL', 'MIDDLE', 'BACKEND', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000007', 'Чем отличается Docker image от container и что такое layer cache?', 'TECHNICAL', 'MIDDLE', 'DEVOPS', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000008', 'Что такое readinessProbe и livenessProbe в Kubernetes?', 'TECHNICAL', 'MIDDLE', 'DEVOPS', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('21000000-0000-0000-0000-000000000009', 'Какие метрики и алерты ты бы поставил для backend-сервиса в production?', 'GENERAL', 'MIDDLE', 'DEVOPS', 'ACTIVE', '10000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
    id, user_id, profile_id, state, current_question_index, started_at, finished_at, created_at, updated_at
)
VALUES
    (
        '40000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002',
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
    id, session_id, external_request_id, status, summary_text, overall_score, raw_payload, requested_at, generated_at, created_at, updated_at
)
VALUES
    (
        '43000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        '42000000-0000-0000-0000-000000000001',
        'READY',
        'Кандидат уверенно отвечает по Spring и PostgreSQL, хорошо рассуждает о диагностике проблем и показывает системный подход.',
        84.50,
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
