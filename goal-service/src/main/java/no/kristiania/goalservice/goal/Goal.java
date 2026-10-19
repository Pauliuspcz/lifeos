package no.kristiania.goalservice.goal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.Instant;

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
    @Column(nullable = false)
    private GoalPeriod period;

    @Column(name = "times_per_period", nullable = false)
    private int timesPerPeriod;

    @Column(name = "amount_per_session")
    private BigDecimal amountPerSession;

    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

    @Column(name = "template_id")
    private Long templateId;

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
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}