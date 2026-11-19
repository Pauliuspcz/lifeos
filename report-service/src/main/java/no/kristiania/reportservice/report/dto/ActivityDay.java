package no.kristiania.reportservice.report.dto;

import java.time.LocalDate;

public record ActivityDay(LocalDate date, int count, int level) { }
