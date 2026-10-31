package no.kristiania.goalservice.goal.dto;

import no.kristiania.goalservice.goal.Goal;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import no.kristiania.goalservice.goal.enums.GoalType;
import no.kristiania.goalservice.goal.enums.TrackingType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record GoalResponse(
        Long id,
        String title,
        String description,
        GoalType goalType,
        TrackingType trackingType,
        GoalPeriod period,
        Integer timesPerPeriod,
        BigDecimal amountPerSession,
        String unit,
        LocalTime targetTime,
        LocalDate startDate,
        LocalDate targetDate,
        String motivation,
        String iconKey,
        String color,
        Long categoryId,
        Long templateId,
        GoalStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
    public static GoalResponse from(Goal g) {
        return new GoalResponse(
                g.getId(), g.getTitle(), g.getDescription(), g.getGoalType(), g.getTrackingType(),
                g.getPeriod(), g.getTimesPerPeriod(), g.getAmountPerSession(), g.getUnit(),
                g.getTargetTime(), g.getStartDate(), g.getTargetDate(), g.getMotivation(),
                g.getIconKey(), g.getColor(), g.getCategoryId(), g.getTemplateId(),
                g.getStatus(), g.getVersion(), g.getCreatedAt(), g.getUpdatedAt(), g.getArchivedAt());
    }
}