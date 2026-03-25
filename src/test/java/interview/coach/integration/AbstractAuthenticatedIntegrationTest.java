package interview.coach.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.coach.repository.UserRepository;
import interview.coach.security.JwtService;
import interview.coach.support.AbstractPostgresIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractAuthenticatedIntegrationTest extends AbstractPostgresIntegrationTest {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    protected static final String ADMIN_EMAIL = "admin@interview-coach.local";
    protected static final String DEMO_EMAIL = "demo@interview-coach.local";
    protected static final String DEFAULT_PASSWORD = "Password123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    protected String accessTokenFor(String email) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = OBJECT_MAPPER.readTree(response);
        return json.get("accessToken").asText();
    }

    protected String accessTokenForSeededUser(String email) {
        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Seeded user not found: " + email));
        return jwtService.generateAccessToken(user).token();
    }
}
