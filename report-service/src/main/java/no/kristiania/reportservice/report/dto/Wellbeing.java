package no.kristiania.reportservice.report.dto;

public record Wellbeing(Double avgMood, Double avgSleep, Double checkinsWellRested,
                        Double checkinsOtherDays, int daysLogged) { }
