package interview.coach.integration;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminQuestionIntegrationTest extends AbstractAuthenticatedIntegrationTest {

    private static final String EXISTING_QUESTION_ID = "21000000-0000-0000-0000-000000000001";

    @Test
    void adminShouldManageQuestionsCrud() throws Exception {
        String adminToken = accessTokenForSeededUser(ADMIN_EMAIL);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(greaterThanOrEqualTo(9)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(9)));

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("query", "postgres")
                        .param("direction", "BACKEND")
                        .param("difficulty", "MIDDLE")
                        .param("questionType", "TECHNICAL")
                        .param("status", "ACTIVE")
                        .param("sortBy", "text")
                        .param("sortDir", "asc")
                        .param("page", "0")
                        .param("size", "10")
                        .param("excludeProfileId", "20000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].text").value("Когда индекс в PostgreSQL помогает, а когда может только замедлить запись?"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "spring")
                        .param("excludeProfileId", "20000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value("21000000-0000-0000-0000-000000000004"));

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));

        mockMvc.perform(get("/admin/questions/{questionId}", EXISTING_QUESTION_ID)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXISTING_QUESTION_ID))
                .andExpect(jsonPath("$.text").isNotEmpty());

        String createResponse = mockMvc.perform(post("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Created from integration test %s",
                                  "questionType": "TECHNICAL",
                                  "difficulty": "MIDDLE",
                                  "direction": "BACKEND",
                                  "status": "ACTIVE"
                                }
                                """.formatted(uniqueSuffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Created from integration test " + uniqueSuffix))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdQuestionId = OBJECT_MAPPER.readTree(createResponse).get("id").asText();

        mockMvc.perform(patch("/admin/questions/{questionId}", createdQuestionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Updated from integration test %s",
                                  "questionType": "GENERAL",
                                  "difficulty": "JUNIOR",
                                  "direction": "DEVOPS",
                                  "status": "DISABLED"
                                }
                                """.formatted(uniqueSuffix)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdQuestionId))
                .andExpect(jsonPath("$.text").value("Updated from integration test " + uniqueSuffix))
                .andExpect(jsonPath("$.questionType").value("GENERAL"))
                .andExpect(jsonPath("$.status").value("DISABLED"));

        mockMvc.perform(delete("/admin/questions/{questionId}", createdQuestionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/admin/questions/{questionId}", createdQuestionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonAdminShouldNotAccessAdminQuestions() throws Exception {
        String demoToken = accessTokenForSeededUser(DEMO_EMAIL);

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());
    }
}
