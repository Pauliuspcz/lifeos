package no.kristiania.reportservice.report.dto;

import no.kristiania.reportservice.report.enums.Recommendation;

public record Suggestion(Recommendation recommendation, Integer suggestedTarget) { }
