package no.kristiania.recommendationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient openMeteoRestClient(@Value("${app.open-meteo.url}") String openMeteoUrl,
                                   @Value("${app.open-meteo.connect-timeout}") Duration connectTimeout,
                                   @Value("${app.open-meteo.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(openMeteoUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
