package no.kristiania.reportservice.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProgressRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "progress_record_gen")
    @SequenceGenerator(name = "progress_record_gen", sequenceName = "progress_record_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Column(nullable = false, unique = true)
    private Long progressId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long goalId;

    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    void onReceive(){
        receivedAt = Instant.now();
    }

}
