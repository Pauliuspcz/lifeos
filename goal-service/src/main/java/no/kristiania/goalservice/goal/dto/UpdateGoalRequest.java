package no.kristiania.goalservice.goal.dto;

import jakarta.validation.constraints.*;
import no.kristiania.goalservice.goal.enums.GoalPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateGoalRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        GoalPeriod period,
        @Min(1) Integer timesPerPeriod,
        @Positive @Digits(integer = 8, fraction = 2) BigDecimal amountPerSession,
        @Size(max = 50) String unit,
        LocalTime targetTime,
        LocalDate startDate,
        LocalDate targetDate,
        @Size(max = 500) String motivation,
        @Size(max = 50) String iconKey,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        Long version
) {}