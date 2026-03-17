--liquibase formatted sql

--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users'

--changeset codex:001-init-schema splitStatements:false
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED'))
);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_status ON users(status);

CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    preferred_direction VARCHAR(50),
    preferred_level VARCHAR(30),
    preferred_language VARCHAR(30),
    interface_language VARCHAR(10),
    theme VARCHAR(20),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_user_preferences_direction CHECK (preferred_direction IS NULL OR preferred_direction IN ('BACKEND', 'FRONTEND', 'DEVOPS')),
    CONSTRAINT chk_user_preferences_level CHECK (preferred_level IS NULL OR preferred_level IN ('JUNIOR', 'MIDDLE'))
);
CREATE INDEX idx_user_preferences_direction ON user_preferences(preferred_direction);
CREATE INDEX idx_user_preferences_level ON user_preferences(preferred_level);

CREATE TABLE interview_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    direction VARCHAR(50) NOT NULL,
    level VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP,
    CONSTRAINT fk_interview_profiles_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_interview_profiles_direction CHECK (direction IN ('BACKEND', 'FRONTEND', 'DEVOPS')),
    CONSTRAINT chk_interview_profiles_level CHECK (level IN ('JUNIOR', 'MIDDLE')),
    CONSTRAINT chk_interview_profiles_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);
CREATE INDEX idx_interview_profiles_direction ON interview_profiles(direction);
CREATE INDEX idx_interview_profiles_level ON interview_profiles(level);
CREATE INDEX idx_interview_profiles_status ON interview_profiles(status);
CREATE INDEX idx_interview_profiles_created_by ON interview_profiles(created_by);

CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    text TEXT NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    difficulty VARCHAR(30),
    direction VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_questions_type CHECK (question_type IN ('TECHNICAL', 'BEHAVIORAL', 'GENERAL')),
    CONSTRAINT chk_questions_difficulty CHECK (difficulty IS NULL OR difficulty IN ('JUNIOR', 'MIDDLE')),
    CONSTRAINT chk_questions_direction CHECK (direction IS NULL OR direction IN ('BACKEND', 'FRONTEND', 'DEVOPS')),
    CONSTRAINT chk_questions_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);
CREATE INDEX idx_questions_created_by ON questions(created_by);
CREATE INDEX idx_questions_direction ON questions(direction);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_status ON questions(status);

CREATE TABLE profile_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    question_id UUID NOT NULL,
    order_index INT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_questions_profile FOREIGN KEY (profile_id) REFERENCES interview_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_questions_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uq_profile_questions_profile_question UNIQUE (profile_id, question_id),
    CONSTRAINT uq_profile_questions_profile_order UNIQUE (profile_id, order_index),
    CONSTRAINT chk_profile_questions_order_index CHECK (order_index >= 0)
);
CREATE INDEX idx_profile_questions_profile_id ON profile_questions(profile_id);
CREATE INDEX idx_profile_questions_question_id ON profile_questions(question_id);

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profile_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    CONSTRAINT fk_profile_tags_profile FOREIGN KEY (profile_id) REFERENCES interview_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_profile_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uq_profile_tags_profile_tag UNIQUE (profile_id, tag_id)
);
CREATE INDEX idx_profile_tags_profile_id ON profile_tags(profile_id);
CREATE INDEX idx_profile_tags_tag_id ON profile_tags(tag_id);

CREATE TABLE interview_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    state VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    current_question_index INT DEFAULT 0,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    last_error_code VARCHAR(100),
    last_error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_sessions_profile FOREIGN KEY (profile_id) REFERENCES interview_profiles(id),
    CONSTRAINT chk_interview_sessions_state CHECK (state IN ('CREATED', 'IN_PROGRESS', 'PAUSED', 'FINISHED', 'PROCESSING', 'REPORT_READY', 'FAILED', 'CANCELED')),
    CONSTRAINT chk_interview_sessions_current_question_index CHECK (current_question_index IS NULL OR current_question_index >= 0)
);
CREATE INDEX idx_interview_sessions_user_id ON interview_sessions(user_id);
CREATE INDEX idx_interview_sessions_profile_id ON interview_sessions(profile_id);
CREATE INDEX idx_interview_sessions_state ON interview_sessions(state);
CREATE INDEX idx_interview_sessions_created_at ON interview_sessions(created_at);

CREATE TABLE session_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    sender_type VARCHAR(30) NOT NULL,
    message_type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    sequence_number INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_messages_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE,
    CONSTRAINT chk_session_messages_sender_type CHECK (sender_type IN ('USER', 'INTERVIEWER', 'SYSTEM')),
    CONSTRAINT chk_session_messages_message_type CHECK (message_type IN ('QUESTION', 'ANSWER', 'INFO', 'ERROR')),
    CONSTRAINT uq_session_messages_sequence UNIQUE (session_id, sequence_number),
    CONSTRAINT chk_session_messages_sequence_number CHECK (sequence_number >= 0)
);
CREATE INDEX idx_session_messages_session_id ON session_messages(session_id);
CREATE INDEX idx_session_messages_created_at ON session_messages(created_at);
CREATE INDEX idx_session_messages_sender_type ON session_messages(sender_type);

CREATE TABLE external_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL,
    request_type VARCHAR(30) NOT NULL,
    request_status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    request_payload JSONB,
    response_payload JSONB,
    error_message TEXT,
    attempt_count INT NOT NULL DEFAULT 0,
    sent_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_external_requests_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE,
    CONSTRAINT chk_external_requests_type CHECK (request_type IN ('NEXT_QUESTION', 'FINAL_REPORT')),
    CONSTRAINT chk_external_requests_status CHECK (request_status IN ('NEW', 'SENT', 'SUCCESS', 'FAILED')),
    CONSTRAINT chk_external_requests_attempt_count CHECK (attempt_count >= 0)
);
CREATE INDEX idx_external_requests_session_id ON external_requests(session_id);
CREATE INDEX idx_external_requests_request_type ON external_requests(request_type);
CREATE INDEX idx_external_requests_request_status ON external_requests(request_status);
CREATE INDEX idx_external_requests_created_at ON external_requests(created_at);

CREATE TABLE session_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL UNIQUE,
    external_request_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    summary_text TEXT,
    overall_score NUMERIC(5,2),
    raw_payload JSONB,
    requested_at TIMESTAMP,
    generated_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_reports_session FOREIGN KEY (session_id) REFERENCES interview_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_reports_external_request FOREIGN KEY (external_request_id) REFERENCES external_requests(id),
    CONSTRAINT chk_session_reports_status CHECK (status IN ('PENDING', 'READY', 'FAILED')),
    CONSTRAINT chk_session_reports_score CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100))
);
CREATE INDEX idx_session_reports_status ON session_reports(status);
CREATE INDEX idx_session_reports_generated_at ON session_reports(generated_at);

CREATE TABLE report_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    title VARCHAR(255),
    content TEXT NOT NULL,
    score NUMERIC(5,2),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_items_report FOREIGN KEY (report_id) REFERENCES session_reports(id) ON DELETE CASCADE,
    CONSTRAINT chk_report_items_type CHECK (item_type IN ('STRENGTH', 'WEAKNESS', 'RECOMMENDATION', 'CATEGORY_SCORE')),
    CONSTRAINT chk_report_items_score CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    CONSTRAINT chk_report_items_sort_order CHECK (sort_order >= 0)
);
CREATE INDEX idx_report_items_report_id ON report_items(report_id);
CREATE INDEX idx_report_items_item_type ON report_items(item_type);
CREATE INDEX idx_report_items_sort_order ON report_items(sort_order);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked_at ON refresh_tokens(revoked_at);
CREATE UNIQUE INDEX uq_refresh_tokens_token_hash ON refresh_tokens(token_hash);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_used_at ON password_reset_tokens(used_at);
CREATE UNIQUE INDEX uq_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_set_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_user_preferences_set_updated_at BEFORE UPDATE ON user_preferences FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_interview_profiles_set_updated_at BEFORE UPDATE ON interview_profiles FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_questions_set_updated_at BEFORE UPDATE ON questions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_interview_sessions_set_updated_at BEFORE UPDATE ON interview_sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_session_reports_set_updated_at BEFORE UPDATE ON session_reports FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO roles (code, name)
VALUES ('USER', 'Пользователь'), ('ADMIN', 'Администратор')
ON CONFLICT (code) DO NOTHING;
