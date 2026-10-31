package no.kristiania.progressservice.progress.dto;

import no.kristiania.progressservice.client.TrackingModel;
import no.kristiania.progressservice.progress.Progress;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressEntryView(Long id, Long goalId, LocalDate date, BigDecimal amount,
                                TrackingModel trackingModel, boolean editable) {
    public static ProgressEntryView from(Progress p, boolean editable) {
        return new ProgressEntryView(p.getId(), p.getGoalId(), p.getDate(),
                p.getAmount(), p.getTrackingModel(), editable);
    }
}
