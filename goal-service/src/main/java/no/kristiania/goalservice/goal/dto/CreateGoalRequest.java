package no.kristiania.goalservice.goal.dto;

import jakarta.validation.constraints.*;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.TrackingType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateGoalRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        TrackingType trackingType,
        GoalPeriod period,
        @Min(1) Integer timesPerPeriod,
        @Positive @Digits(integer = 10, fraction = 2) BigDecimal targetAmount,
        @Size(max = 50) String unit,
        LocalTime targetTime,
        LocalDate startDate,
        LocalDate targetDate,
        Long categoryId,
        Long templateId,
        @Size(max = 500) String motivation,
        @Size(max = 50) String iconKey,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        Boolean outdoor
) {}