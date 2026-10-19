package no.kristiania.goalservice.goal.dto;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record GoalResponse(
        Long id,
        String title,
        String description,
        GoalPeriod period,
        int timesPerPeriod,
        BigDecimal amountPerSession,
        String unit,
        GoalStatus status,
        Long templateId,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(), goal.getTitle(), goal.getDescription(), goal.getPeriod(),
                goal.getTimesPerPeriod(), goal.getAmountPerSession(), goal.getUnit(),
                goal.getStatus(), goal.getTemplateId(), goal.getVersion(),
                goal.getCreatedAt(), goal.getUpdatedAt(), goal.getArchivedAt());
    }
}
