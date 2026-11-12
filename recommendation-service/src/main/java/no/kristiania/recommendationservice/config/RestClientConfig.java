package no.kristiania.recommendationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient openMeteoRestClient(@Value("${app.open-meteo.url}") String openMeteoUrl) {
        return RestClient.builder()
                .baseUrl(openMeteoUrl)
                .build();
    }
}
