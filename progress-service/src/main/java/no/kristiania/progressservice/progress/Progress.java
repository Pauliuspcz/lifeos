package no.kristiania.progressservice.progress;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.kristiania.progressservice.client.TrackingModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "progress_gen")
    @SequenceGenerator(name = "progress_gen", sequenceName = "progress_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID eventId; // unique id sent in the RabbitMQ event

    @Column(nullable = false)
    private Long userId; //hardcoded before user finished

    @Column(nullable = false)
    private Long goalId; // Which goal it counts for

    private BigDecimal amount; //how much was done

    @Column(nullable = false)
    private LocalDate date; //the day the session counts for

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TrackingModel trackingModel;

    @PrePersist
    void onCreate() {
        eventId = UUID.randomUUID();
        createdAt = Instant.now();
    }
}
