package no.kristiania.goalservice.integration.setup;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static no.kristiania.goalservice.integration.setup.JwtTestSupport.ISSUER;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.KEYS;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.toBase64Pem;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Value("${local.server.port}") protected int port;

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }

    @BeforeEach
    protected void cleanDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "goals");
    }
}