package no.kristiania.goalservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static no.kristiania.goalservice.testSupport.JwtTestSupport.ISSUER;
import static no.kristiania.goalservice.testSupport.JwtTestSupport.KEYS;
import static no.kristiania.goalservice.testSupport.JwtTestSupport.toBase64Pem;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @BeforeEach
    void cleanDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "goals");
    }
}