package no.kristiania.progressservice.progress.dto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProgressRequest(
        @NotNull Long goalId,
        @NotNull @PastOrPresent LocalDate date,
        @Positive @Digits(integer = 8, fraction = 2) BigDecimal amount
) {
}
