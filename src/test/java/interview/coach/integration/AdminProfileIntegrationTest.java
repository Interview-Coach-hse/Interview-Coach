package interview.coach.integration;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminProfileIntegrationTest extends AbstractAuthenticatedIntegrationTest {

    @Test
    void adminShouldReadDraftProfileById() throws Exception {
        String adminToken = accessTokenForSeededUser(ADMIN_EMAIL);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        String createResponse = mockMvc.perform(post("/admin/profiles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Draft profile %s",
                                  "description": "Draft profile for admin read test",
                                  "direction": "FRONTEND",
                                  "level": "JUNIOR",
                                  "tags": ["draft", "admin"]
                                }
                                """.formatted(uniqueSuffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String profileId = OBJECT_MAPPER.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/profiles/{profileId}", profileId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/admin/profiles/{profileId}", profileId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value("Draft profile " + uniqueSuffix));
    }

    @Test
    void nonAdminShouldNotReadDraftProfileByAdminEndpoint() throws Exception {
        String demoToken = accessTokenForSeededUser(DEMO_EMAIL);

        mockMvc.perform(get("/admin/profiles/{profileId}", "20000000-0000-0000-0000-000000000001")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());
    }
}
