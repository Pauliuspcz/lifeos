package no.kristiania.goalservice.integration.security;

import no.kristiania.goalservice.integration.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static no.kristiania.goalservice.integration.setup.JwtTestSupport.bearer;
import static no.kristiania.goalservice.integration.setup.JwtTestSupport.tokenFor;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorPageTest extends AbstractIntegrationTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void unknownRoute_withValidToken_returns404_not401() throws Exception {
        HttpResponse<String> response = send("GET", "/api/does-not-exist");

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    void wrongMethod_withValidToken_returns405_not401() throws Exception {
        HttpResponse<String> response = send("DELETE", "/api/goal");

        assertThat(response.statusCode()).isEqualTo(405);
    }

    private HttpResponse<String> send(String method, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .header("Authorization", bearer(tokenFor(1L)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
