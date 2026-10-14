package no.kristiania.lifeos.integration;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.lifeos.auth.google.GoogleTokenVerifier;
import no.kristiania.lifeos.auth.token.RefreshTokenRepository;
import no.kristiania.lifeos.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;



import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @DynamicPropertySource
    static void jwtKeys(DynamicPropertyRegistry registry) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        registry.add("app.jwt.private-key", () -> toBase64Pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        registry.add("app.jwt.public-key", () -> toBase64Pem("PUBLIC KEY", pair.getPublic().getEncoded()));
    }

    private static String toBase64Pem(String type, byte[] der) {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der)
                + "\n-----END " + type + "-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(StandardCharsets.UTF_8));
    }

    protected static final String PASSWORD = "password123";

    protected record Tokens(String access, String refresh) {}

    @Autowired protected JdbcTemplate jdbcTemplate;

    protected ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path).contentType(APPLICATION_JSON).content(body));
    }

    protected String credentials(String email, String password) {
        return """
            {"email":"%s","password":"%s"}""".formatted(email, password);
    }

    protected void register(String email, String password) throws Exception {
        postJson("/auth/register", """
            {"email":"%s","password":"%s","name":"Kari"}""".formatted(email, password))
                .andExpect(status().isCreated());
    }

    protected Tokens tokensFrom(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return new Tokens(JsonPath.read(body, "$.accessToken"), JsonPath.read(body, "$.refreshToken"));
    }

    protected Tokens registerAndLogin(String email) throws Exception {
        register(email, PASSWORD);
        return tokensFrom(postJson("/auth/login", credentials(email, PASSWORD))
                .andExpect(status().isOk()).andReturn());
    }

    protected ResultActions refresh(String refreshToken) throws Exception {
        return postJson("/auth/refresh", """
            {"refreshToken":"%s"}""".formatted(refreshToken));
    }

    protected MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    protected ResultActions profileOf(Tokens tokens) throws Exception {
        return mockMvc.perform(authed(get("/profile/me"), tokens.access()));
    }

    protected int userCount() {
        return jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
    }

    @MockitoBean
    protected GoogleTokenVerifier googleTokenVerifier;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }
}
