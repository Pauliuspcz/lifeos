package no.kristiania.progressservice.progress.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalProgressSummary(Long goalId, BigDecimal total, BigDecimal target,
                                  String unit, LocalDate startDate, LocalDate targetDate,
                                  BigDecimal expected, BigDecimal delta) {}
