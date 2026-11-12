package no.kristiania.recommendationservice.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import no.kristiania.recommendationservice.weather.CachedWeather;
import no.kristiania.recommendationservice.weather.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    Cache<Location, CachedWeather> weatherCache(@Value("${app.weather.cache.fresh-ttl}") Duration staleMax,
                                                @Value("${app.weather.cache.max-entries}") long maxEntries) {
        return Caffeine.newBuilder()
                .expireAfterWrite(staleMax)
                .maximumSize(maxEntries)
                .build();
    }
}
