package interview.coach.exception;

import org.springframework.http.HttpStatus;

public class AssessmentIntegrationException extends ApiException {

    public static final String ERROR_CODE = "ASSESSMENT_SERVICE_UNAVAILABLE";
    public static final String DEFAULT_MESSAGE = "External assessment service is unavailable. Please try again later.";

    public AssessmentIntegrationException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, ERROR_CODE, DEFAULT_MESSAGE);
    }

    public AssessmentIntegrationException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, ERROR_CODE, message == null || message.isBlank() ? DEFAULT_MESSAGE : message);
    }
}
