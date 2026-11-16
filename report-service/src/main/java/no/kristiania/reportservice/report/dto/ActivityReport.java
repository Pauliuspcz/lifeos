package no.kristiania.reportservice.report.dto;

import java.util.List;

public record ActivityReport(int totalCheckins, int activeDays, List<ActivityDay> days) { }
