package no.kristiania.reportservice.report.dto;

import java.time.LocalDate;

public record PeriodCount(LocalDate periodStart, int count, boolean reached) { }
