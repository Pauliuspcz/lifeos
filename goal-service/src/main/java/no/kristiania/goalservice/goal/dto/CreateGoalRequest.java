package no.kristiania.goalservice.goal.dto;

import jakarta.validation.constraints.*;
import no.kristiania.goalservice.goal.enums.GoalPeriod;

import java.math.BigDecimal;

public record CreateGoalRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String description,
        @NotNull GoalPeriod period,
        @NotNull @Min(1) Integer timesPerPeriod,
        @Positive @Digits(integer = 8, fraction = 2) BigDecimal amountPerSession,
        @Size(max = 50) String unit
) {}
