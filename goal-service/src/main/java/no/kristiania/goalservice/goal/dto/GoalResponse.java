package no.kristiania.goalservice.goal.dto;

import no.kristiania.goalservice.goal.entity.Goal;
import no.kristiania.goalservice.shared.enums.GoalPeriod;
import no.kristiania.goalservice.goal.entity.GoalStatus;
import no.kristiania.goalservice.shared.enums.TrackingType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record GoalResponse(
        Long id,
        String title,
        String description,
        TrackingType trackingType,
        GoalPeriod period,
        Integer timesPerPeriod,
        BigDecimal targetAmount,
        String unit,
        LocalTime targetTime,
        LocalDate startDate,
        LocalDate targetDate,
        boolean outdoor,
        String motivation,
        String iconKey,
        String color,
        Long categoryId,
        Long templateId,
        String templateCode,
        String directionCode,
        GoalStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
    public static GoalResponse from(Goal g) {
        return from(g, null, null);
    }

    public static GoalResponse from(Goal g, String templateCode, String directionCode) {
        return new GoalResponse(
                g.getId(), g.getTitle(), g.getDescription(), g.getTrackingType(),
                g.getPeriod(), g.getTimesPerPeriod(), g.getTargetAmount(), g.getUnit(),
                g.getTargetTime(), g.getStartDate(), g.getTargetDate(), g.isOutdoor(),
                g.getMotivation(), g.getIconKey(), g.getColor(), g.getCategoryId(), g.getTemplateId(),
                templateCode, directionCode,
                g.getStatus(), g.getVersion(), g.getCreatedAt(), g.getUpdatedAt(), g.getArchivedAt());
    }
}