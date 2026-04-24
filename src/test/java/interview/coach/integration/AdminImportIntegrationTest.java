package interview.coach.integration;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminImportIntegrationTest extends AbstractAuthenticatedIntegrationTest {

    @Test
    void adminShouldImportQuestionsJson() throws Exception {
        String adminToken = accessTokenForSeededUser(ADMIN_EMAIL);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "questions.json",
                "application/json",
                """
                [
                  {
                    "text": "Existing import question",
                    "questionType": "TECHNICAL",
                    "difficulty": "JUNIOR",
                    "direction": "BACKEND",
                    "status": "ACTIVE"
                  },
                  {
                    "text": "Imported unique question %s",
                    "questionType": "BEHAVIORAL",
                    "difficulty": "MIDDLE",
                    "direction": "DEVOPS",
                    "status": "ACTIVE"
                  }
                ]
                """.formatted(uniqueSuffix).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(post("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Existing import question",
                                  "questionType": "TECHNICAL",
                                  "difficulty": "JUNIOR",
                                  "direction": "BACKEND",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(multipart("/admin/import")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("QUESTIONS"))
                .andExpect(jsonPath("$.profileId").doesNotExist())
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.createdQuestions").value(1))
                .andExpect(jsonPath("$.reusedQuestions").value(1))
                .andExpect(jsonPath("$.linkedQuestions").value(0));

        mockMvc.perform(get("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("query", "Imported unique question " + uniqueSuffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].questionType").value("BEHAVIORAL"))
                .andExpect(jsonPath("$.items[0].direction").value("DEVOPS"));
    }

    @Test
    void adminShouldImportProfileWithQuestionsJson() throws Exception {
        String adminToken = accessTokenForSeededUser(ADMIN_EMAIL);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.json",
                "application/json",
                """
                {
                  "profile": {
                    "title": "Imported profile %s",
                    "description": "Profile imported from JSON",
                    "direction": "BACKEND",
                    "level": "MIDDLE",
                    "tags": ["json-import", "backend"]
                  },
                  "questions": [
                    {
                      "text": "Imported profile question %s",
                      "questionType": "TECHNICAL",
                      "required": true
                    },
                    {
                      "text": "Imported profile question reused %s",
                      "questionType": "GENERAL",
                      "orderIndex": 5,
                      "required": false
                    }
                  ]
                }
                """.formatted(uniqueSuffix, uniqueSuffix, uniqueSuffix).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(post("/admin/questions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Imported profile question reused %s",
                                  "questionType": "GENERAL",
                                  "difficulty": "MIDDLE",
                                  "direction": "BACKEND",
                                  "status": "ACTIVE"
                                }
                                """.formatted(uniqueSuffix)))
                .andExpect(status().isOk());

        String response = mockMvc.perform(multipart("/admin/import")
                        .file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("PROFILE"))
                .andExpect(jsonPath("$.profileTitle").value("Imported profile " + uniqueSuffix))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.createdQuestions").value(1))
                .andExpect(jsonPath("$.reusedQuestions").value(1))
                .andExpect(jsonPath("$.linkedQuestions").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String profileId = OBJECT_MAPPER.readTree(response).get("profileId").asText();

        mockMvc.perform(get("/admin/profiles/{profileId}", profileId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Imported profile " + uniqueSuffix))
                .andExpect(jsonPath("$.direction").value("BACKEND"))
                .andExpect(jsonPath("$.level").value("MIDDLE"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.tags.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.questions.length()").value(2))
                .andExpect(jsonPath("$.questions[0].questionText").value("Imported profile question " + uniqueSuffix))
                .andExpect(jsonPath("$.questions[0].orderIndex").value(0))
                .andExpect(jsonPath("$.questions[1].questionText").value("Imported profile question reused " + uniqueSuffix))
                .andExpect(jsonPath("$.questions[1].orderIndex").value(5));
    }

    @Test
    void nonAdminShouldNotImportJson() throws Exception {
        String demoToken = accessTokenForSeededUser(DEMO_EMAIL);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "questions.json",
                "application/json",
                """
                [
                  { "text": "Question from forbidden import" }
                ]
                """.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/admin/import")
                        .file(file)
                        .header("Authorization", "Bearer " + demoToken))
                .andExpect(status().isForbidden());
    }
}
