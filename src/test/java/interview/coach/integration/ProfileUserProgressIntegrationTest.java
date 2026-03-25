package interview.coach.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileUserProgressIntegrationTest extends AbstractAuthenticatedIntegrationTest {

    private static final String BACKEND_MIDDLE_PROFILE_ID = "20000000-0000-0000-0000-000000000002";

    @Test
    void publicCatalogShouldFilterAndExposePublishedProfileDetails() throws Exception {
        mockMvc.perform(get("/profiles")
                        .param("direction", "BACKEND")
                        .param("level", "MIDDLE")
                        .param("tag", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(BACKEND_MIDDLE_PROFILE_ID))
                .andExpect(jsonPath("$.items[0].title").value("Backend Java Middle"));

        mockMvc.perform(get("/profiles/{profileId}", BACKEND_MIDDLE_PROFILE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BACKEND_MIDDLE_PROFILE_ID))
                .andExpect(jsonPath("$.questions.length()").value(3))
                .andExpect(jsonPath("$.tags.length()").value(3));
    }

    @Test
    void authenticatedUserShouldUpdateProfileAndSeeProgress() throws Exception {
        String demoToken = accessTokenForSeededUser(DEMO_EMAIL);

        mockMvc.perform(patch("/user")
                        .header("Authorization", "Bearer " + demoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Updated",
                                  "lastName": "Demo",
                                  "preferredDirection": "DEVOPS",
                                  "preferredLevel": "MIDDLE",
                                  "preferredLanguage": "en",
                                  "interfaceLanguage": "en",
                                  "theme": "dark"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Demo"))
                .andExpect(jsonPath("$.preference.preferredDirection").value("DEVOPS"))
                .andExpect(jsonPath("$.preference.theme").value("dark"));

        mockMvc.perform(get("/user")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(DEMO_EMAIL))
                .andExpect(jsonPath("$.preference.interfaceLanguage").value("en"));

        mockMvc.perform(get("/progress")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSessions").value(2))
                .andExpect(jsonPath("$.finishedSessions").value(1))
                .andExpect(jsonPath("$.reportsReady").value(1))
                .andExpect(jsonPath("$.averageScore").value(84.50));
    }
}
