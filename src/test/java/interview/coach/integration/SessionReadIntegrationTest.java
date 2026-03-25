package interview.coach.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionReadIntegrationTest extends AbstractAuthenticatedIntegrationTest {

    private static final String FINISHED_SESSION_ID = "40000000-0000-0000-0000-000000000001";

    @Test
    void demoUserShouldSeeHistorySessionMessagesAndReport() throws Exception {
        String demoToken = accessTokenForSeededUser(DEMO_EMAIL);

        mockMvc.perform(get("/history/sessions")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items.length()").value(2));

        mockMvc.perform(get("/sessions/{sessionId}", FINISHED_SESSION_ID)
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FINISHED_SESSION_ID))
                .andExpect(jsonPath("$.state").value("FINISHED"))
                .andExpect(jsonPath("$.profileTitle").value("Backend Java Middle"));

        mockMvc.perform(get("/sessions/{sessionId}/messages", FINISHED_SESSION_ID)
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(6))
                .andExpect(jsonPath("$.items[0].messageType").value("QUESTION"))
                .andExpect(jsonPath("$.items[1].messageType").value("ANSWER"));

        mockMvc.perform(get("/sessions/{sessionId}/report", FINISHED_SESSION_ID)
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.overallScore").value(84.50))
                .andExpect(jsonPath("$.items.length()").value(5));
    }
}
