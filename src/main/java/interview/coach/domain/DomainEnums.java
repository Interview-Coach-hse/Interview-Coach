package interview.coach.domain;

public final class DomainEnums {

    private DomainEnums() {
    }

    public enum UserStatus {
        ACTIVE,
        BLOCKED,
        DELETED
    }

    public enum InterviewDirection {
        BACKEND,
        FRONTEND,
        DEVOPS
    }

    public enum InterviewLevel {
        JUNIOR,
        MIDDLE
    }

    public enum ProfileStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    public enum QuestionType {
        TECHNICAL,
        BEHAVIORAL,
        GENERAL
    }

    public enum QuestionStatus {
        ACTIVE,
        DISABLED
    }

    public enum SessionState {
        CREATED,
        IN_PROGRESS,
        PAUSED,
        FINISHED,
        PROCESSING,
        FAILED,
        CANCELED
    }

    public enum SenderType {
        USER,
        INTERVIEWER,
        SYSTEM
    }

    public enum MessageType {
        QUESTION,
        ANSWER,
        INFO,
        ERROR
    }

    public enum ExternalRequestType {
        NEXT_QUESTION,
        FINAL_REPORT
    }

    public enum ExternalRequestStatus {
        NEW,
        SENT,
        SUCCESS,
        FAILED
    }

    public enum ReportStatus {
        PENDING,
        READY,
        FAILED
    }

    public enum ReportItemType {
        STRENGTH,
        WEAKNESS,
        RECOMMENDATION,
        CATEGORY_SCORE
    }

    public enum VerificationPurpose {
        REGISTRATION,
        EMAIL_CHANGE
    }
}
