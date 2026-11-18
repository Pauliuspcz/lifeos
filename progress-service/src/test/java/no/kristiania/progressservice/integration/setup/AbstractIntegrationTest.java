package no.kristiania.progressservice.integration.setup;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static no.kristiania.progressservice.integration.setup.JwtTestSupport.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.public-key",
                () -> toBase64Pem("PUBLIC KEY", KEYS.getPublic().getEncoded()));
        registry.add("app.jwt.issuer", () -> ISSUER);
    }
}
