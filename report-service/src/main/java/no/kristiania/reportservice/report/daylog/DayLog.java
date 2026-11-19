package no.kristiania.reportservice.report.daylog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DayLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "day_log_gen")
    @SequenceGenerator(name = "day_log_gen", sequenceName = "day_log_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    private Integer mood;

    private Integer sleepHours;
}
