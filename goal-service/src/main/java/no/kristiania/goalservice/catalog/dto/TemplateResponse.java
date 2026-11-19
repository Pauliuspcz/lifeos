package no.kristiania.goalservice.catalog.dto;

import no.kristiania.goalservice.catalog.entity.Difficulty;
import no.kristiania.goalservice.catalog.entity.GoalTemplate;
import no.kristiania.goalservice.shared.enums.GoalPeriod;
import no.kristiania.goalservice.shared.enums.TrackingType;

import java.math.BigDecimal;
import java.time.LocalTime;

public record TemplateResponse(Long id, String code, Long categoryId, Long directionId,
                               String title, String description, TrackingType trackingType,
                               GoalPeriod defaultPeriod, Integer defaultTimesPerPeriod,
                               BigDecimal defaultTargetAmount, String defaultUnit,
                               LocalTime defaultTargetTime, Integer defaultDurationDays,
                               boolean outdoor, Difficulty difficulty,
                               String iconKey, String color) {
    public static TemplateResponse from(GoalTemplate t) {
        return new TemplateResponse(t.getId(), t.getCode(), t.getCategoryId(), t.getDirectionId(),
                t.getTitle(), t.getDescription(), t.getTrackingType(), t.getDefaultPeriod(),
                t.getDefaultTimesPerPeriod(), t.getDefaultTargetAmount(), t.getDefaultUnit(),
                t.getDefaultTargetTime(), t.getDefaultDurationDays(), t.isOutdoor(),
                t.getDifficulty(), t.getIconKey(), t.getColor());
    }
}