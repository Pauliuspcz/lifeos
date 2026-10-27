package no.kristiania.progressservice.progress.dto;

import no.kristiania.progressservice.progress.Progress;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProgressResponse(
        Long id,
        Long goalId,
        LocalDate date,
        BigDecimal amount,
        Instant createdAt
) {
    public static ProgressResponse from(Progress progress) {
        return new ProgressResponse(progress.getId(), progress.getGoalId(),
                progress.getDate(), progress.getAmount(), progress.getCreatedAt());
    }
}
