package interview.coach.integration.assessment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class AssessmentDtos {

    private AssessmentDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record QuestionsResponse(
            String status,
            String specialization,
            String grade,
            Integer count,
            List<QuestionItem> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record QuestionItem(
            String questionId,
            String specialization,
            String grade,
            String topicCode,
            String topicLabel,
            String questionText,
            List<String> tags,
            String version
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ReportRequest(
            String requestId,
            String sessionId,
            String clientId,
            String mode,
            Scenario scenario,
            List<ReportItemRequest> items,
            Metadata metadata
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Scenario(
            String scenarioId,
            String specialization,
            String grade,
            List<String> topics,
            String reportLanguage
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ReportItemRequest(
            String itemId,
            String questionId,
            String questionText,
            String answerText,
            String askedAt,
            List<String> tags
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Metadata(
            String source,
            String subscriptionPlan
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ReportResponse(
            String status,
            Job job,
            Report report
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Job(
            String status,
            String jobId,
            String requestId,
            String sessionId,
            String createdAt,
            String updatedAt,
            String errorCode,
            String errorMessage
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Report(
            String requestId,
            String sessionId,
            String clientId,
            String specialization,
            String grade,
            BigDecimal overallScore,
            Map<String, BigDecimal> criterionScores,
            String summary,
            List<QuestionReport> questions,
            List<TopicReport> topics,
            List<String> recommendations,
            Versions versions,
            String generatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record QuestionReport(
            String itemId,
            String questionId,
            String questionText,
            String topic,
            BigDecimal score,
            Map<String, BigDecimal> criterionScores,
            String summary,
            List<String> strengths,
            List<String> issues,
            List<String> coveredKeypoints,
            List<String> missingKeypoints,
            List<String> detectedMistakes,
            List<String> recommendations,
            List<ContextSnippet> contextSnippets
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ContextSnippet(
            String chunkId,
            String sourceTitle,
            String sourceUrl,
            String excerpt,
            BigDecimal score
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record TopicReport(
            String topic,
            BigDecimal averageScore,
            List<String> strengths,
            List<String> gaps
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Versions(
            String modelVersion,
            String rubricVersion,
            String kbVersion,
            String questionsVersion,
            String promptVersion
    ) {
    }
}
