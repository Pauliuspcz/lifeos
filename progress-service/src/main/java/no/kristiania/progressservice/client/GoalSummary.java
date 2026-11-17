package no.kristiania.progressservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoalSummary(Long id, String status, String trackingType,
                          Integer timesPerPeriod, String period,
                          BigDecimal targetAmount, String unit,
                          LocalDate startDate, LocalDate targetDate) {}