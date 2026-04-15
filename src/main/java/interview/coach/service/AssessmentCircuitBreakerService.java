package interview.coach.service;

import interview.coach.config.AssessmentClientProperties;
import interview.coach.exception.AssessmentIntegrationException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssessmentCircuitBreakerService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentCircuitBreakerService.class);

    private final Clock clock;
    private final int failureThreshold;
    private final long openDurationSeconds;

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private Instant openUntil;
    private boolean halfOpenProbeInFlight = false;

    public AssessmentCircuitBreakerService(AssessmentClientProperties properties, Clock clock) {
        this.clock = clock;
        this.failureThreshold = Math.max(1, properties.circuitBreakerFailureThreshold());
        this.openDurationSeconds = Math.max(1L, properties.circuitBreakerOpenDurationSeconds());
    }

    public synchronized void acquirePermission() {
        Instant now = clock.instant();
        if (state == State.OPEN) {
            if (openUntil != null && now.isBefore(openUntil)) {
                throw new AssessmentIntegrationException("External assessment service is temporarily unavailable. Circuit breaker is open.");
            }
            state = State.HALF_OPEN;
            halfOpenProbeInFlight = false;
            log.info("Assessment circuit breaker switched to HALF_OPEN");
        }

        if (state == State.HALF_OPEN) {
            if (halfOpenProbeInFlight) {
                throw new AssessmentIntegrationException("External assessment service is temporarily unavailable. Circuit breaker is open.");
            }
            halfOpenProbeInFlight = true;
        }
    }

    public synchronized void recordSuccess() {
        if (state != State.CLOSED || consecutiveFailures != 0) {
            log.info("Assessment circuit breaker closed after successful call");
        }
        state = State.CLOSED;
        consecutiveFailures = 0;
        openUntil = null;
        halfOpenProbeInFlight = false;
    }

    public synchronized void recordFailure(Exception exception) {
        if (state == State.HALF_OPEN) {
            openCircuit();
            log.warn("Assessment circuit breaker re-opened after failed HALF_OPEN probe: {}", safeMessage(exception));
            return;
        }

        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) {
            openCircuit();
            log.warn("Assessment circuit breaker opened after {} consecutive failure(s): {}", consecutiveFailures, safeMessage(exception));
            return;
        }

        halfOpenProbeInFlight = false;
        log.warn("Assessment circuit breaker recorded failure {}/{}: {}", consecutiveFailures, failureThreshold, safeMessage(exception));
    }

    synchronized StateSnapshot snapshot() {
        return new StateSnapshot(state, consecutiveFailures, openUntil, halfOpenProbeInFlight);
    }

    private void openCircuit() {
        state = State.OPEN;
        openUntil = clock.instant().plusSeconds(openDurationSeconds);
        consecutiveFailures = 0;
        halfOpenProbeInFlight = false;
    }

    private String safeMessage(Exception exception) {
        return Objects.requireNonNullElse(exception.getMessage(), exception.getClass().getSimpleName());
    }

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    record StateSnapshot(
            State state,
            int consecutiveFailures,
            Instant openUntil,
            boolean halfOpenProbeInFlight
    ) {
    }
}
