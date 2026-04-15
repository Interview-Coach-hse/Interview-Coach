--liquibase formatted sql

--changeset codex:004-replace-report-ready-with-finished splitStatements:false
UPDATE interview_sessions
SET state = 'FINISHED'
WHERE state = 'REPORT_READY';

ALTER TABLE interview_sessions
    DROP CONSTRAINT IF EXISTS chk_interview_sessions_state;

ALTER TABLE interview_sessions
    ADD CONSTRAINT chk_interview_sessions_state
        CHECK (state IN ('CREATED', 'IN_PROGRESS', 'PAUSED', 'FINISHED', 'PROCESSING', 'FAILED', 'CANCELED'));
