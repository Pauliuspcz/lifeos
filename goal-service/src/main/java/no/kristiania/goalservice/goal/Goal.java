package no.kristiania.goalservice.goal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.kristiania.goalservice.goal.enums.GoalPeriod;
import no.kristiania.goalservice.goal.enums.GoalStatus;
import java.math.BigDecimal;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goal_gen")
    @SequenceGenerator(name = "goal_gen", sequenceName = "goal_seq", allocationSize = 1)
    @Column(name = "id")
    private Long goalId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private int targetCount; //amount of times you must complete the goal within a period, 3x/week = 3.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalPeriod period;

    private BigDecimal targetValue; //amount required per completion

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE; //active by default

}
