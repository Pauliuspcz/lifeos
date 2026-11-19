package no.kristiania.goalservice.goal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.kristiania.goalservice.shared.enums.GoalPeriod;
import no.kristiania.goalservice.shared.enums.TrackingType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_type", nullable = false)
    private TrackingType trackingType = TrackingType.CHECK;

    @Enumerated(EnumType.STRING)
    private GoalPeriod period;

    @Column(name = "times_per_period")
    private Integer timesPerPeriod;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    private String unit;

    @Column(name = "target_time")
    private LocalTime targetTime;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(nullable = false)
    private boolean outdoor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "template_id")
    private Long templateId;

    private String motivation;

    @Column(name = "icon_key")
    private String iconKey;

    private String color;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}