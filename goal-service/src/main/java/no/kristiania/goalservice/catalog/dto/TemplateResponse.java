package no.kristiania.goalservice.catalog.dto;

import no.kristiania.goalservice.catalog.Difficulty;
import no.kristiania.goalservice.catalog.GoalTemplate;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalType;
import no.kristiania.goalservice.goal.enums.TrackingType;

import java.math.BigDecimal;
import java.time.LocalTime;

public record TemplateResponse(Long id, Long categoryId, String title, String description,
                               GoalType goalType, TrackingType trackingType,
                               GoalPeriod defaultPeriod, Integer defaultTimesPerPeriod,
                               BigDecimal defaultTargetAmount, String defaultUnit,
                               LocalTime defaultTargetTime, Difficulty difficulty,
                               String iconKey, String color) {
    public static TemplateResponse from(GoalTemplate t) {
        return new TemplateResponse(t.getId(), t.getCategoryId(), t.getTitle(), t.getDescription(),
                t.getGoalType(), t.getTrackingType(), t.getDefaultPeriod(),
                t.getDefaultTimesPerPeriod(), t.getDefaultTargetAmount(), t.getDefaultUnit(),
                t.getDefaultTargetTime(), t.getDifficulty(), t.getIconKey(), t.getColor());
    }
}
