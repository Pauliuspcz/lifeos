package no.kristiania.progressservice.progress.dto;

import no.kristiania.progressservice.progress.entity.TrackingModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ProgressWindowResponse(LocalDate today, LocalDate weekStart, LocalDate monthStart, LocalDate from,
                                     Map<TrackingModel, DateRange> editable,
                                     List<ProgressEntryView> entries) {}

