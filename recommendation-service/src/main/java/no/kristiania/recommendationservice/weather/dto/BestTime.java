package no.kristiania.recommendationservice.weather.dto;

import no.kristiania.recommendationservice.weather.enums.Rating;
import java.time.LocalDateTime;

public record BestTime(LocalDateTime from, LocalDateTime to, Rating rating, double temperature) {}
