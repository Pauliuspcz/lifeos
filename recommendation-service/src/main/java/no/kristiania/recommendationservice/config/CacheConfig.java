package no.kristiania.recommendationservice.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import no.kristiania.recommendationservice.weather.CachedWeather;
import no.kristiania.recommendationservice.weather.Location;
import no.kristiania.recommendationservice.weather.props.WeatherCacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    Cache<Location, CachedWeather> weatherCache(WeatherCacheProperties weatherCacheProperties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(weatherCacheProperties.staleMax())
                .maximumSize(weatherCacheProperties.maxEntries())
                .build();
    }
}
