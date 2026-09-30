package no.kristiania.goalservice.goal.dto;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import java.math.BigDecimal;

public record CreateGoalRequest(
        String title,
        String description,
        GoalPeriod period,
        int timesPerPeriod,
        BigDecimal amountPerSession


) {}
