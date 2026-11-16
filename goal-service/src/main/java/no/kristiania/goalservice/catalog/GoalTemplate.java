package no.kristiania.goalservice.catalog;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.TrackingType;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Immutable
@Table(name = "goal_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalTemplate {

    @Id
    private Long id;

    private String code;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "direction_id")
    private Long directionId;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type")
    private TrackingType trackingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_period")
    private GoalPeriod defaultPeriod;

    @Column(name = "default_times_per_period")
    private Integer defaultTimesPerPeriod;

    @Column(name = "default_target_amount")
    private BigDecimal defaultTargetAmount;

    @Column(name = "default_unit")
    private String defaultUnit;

    @Column(name = "default_target_time")
    private LocalTime defaultTargetTime;

    @Column(name = "default_duration_days")
    private Integer defaultDurationDays;

    private boolean outdoor;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(name = "icon_key")
    private String iconKey;

    private String color;

    @Column(name = "sort_order")
    private int sortOrder;

    private boolean active;
}