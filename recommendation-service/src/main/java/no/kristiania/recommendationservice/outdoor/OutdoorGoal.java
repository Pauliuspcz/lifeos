package no.kristiania.recommendationservice.outdoor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outdoor_goal")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutdoorGoal {
    @Id
    private Long goalId;

    @Column(nullable = false)
    private Long userId;
}
