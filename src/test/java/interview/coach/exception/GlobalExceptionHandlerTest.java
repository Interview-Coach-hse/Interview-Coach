package interview.coach.exception;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiShouldExposeErrorCodeForIntegrationFailures() {
        var response = handler.handleApi(new AssessmentIntegrationException());

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("code", AssessmentIntegrationException.ERROR_CODE);
        assertThat(response.getBody()).containsEntry("message", AssessmentIntegrationException.DEFAULT_MESSAGE);
    }

    @Test
    void handleApiShouldOmitCodeWhenApiExceptionHasNoCode() {
        var response = handler.handleApi(new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Bad request"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).doesNotContainKey("code");
        assertThat(response.getBody()).containsEntry("message", "Bad request");
    }
}
