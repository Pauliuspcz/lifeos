package no.kristiania.reportservice.unit.report;

import no.kristiania.reportservice.report.ProgressEventListener;
import no.kristiania.reportservice.report.ProgressRecord;
import no.kristiania.reportservice.report.ProgressRecordRepo;
import no.kristiania.reportservice.report.enums.TrackingModel;
import no.kristiania.reportservice.report.events.ProgressCreatedEvent;
import no.kristiania.reportservice.report.events.ProgressDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressEventListenerTest {
    @Mock
    ProgressRecordRepo repo;
    @InjectMocks
    ProgressEventListener listener;

    private static ProgressCreatedEvent created(UUID eventId) {
        return new ProgressCreatedEvent(eventId, 5L, 7L, 33L, LocalDate.of(2026, 11, 8),
                null, TrackingModel.WEEKLY_COUNT, 2, "WEEKLY", Instant.now());
    }

    @Test
    void savesCreatedEventWithAllFields() {
        UUID eventId = UUID.randomUUID();
        when(repo.existsByEventId(eventId)).thenReturn(false);

        listener.onProgressCreated(created(eventId));

        ArgumentCaptor<ProgressRecord> saved = ArgumentCaptor.forClass(ProgressRecord.class);
        verify(repo).save(saved.capture());
        ProgressRecord r = saved.getValue();
        assertThat(r.getEventId()).isEqualTo(eventId);
        assertThat(r.getProgressId()).isEqualTo(5L);
        assertThat(r.getUserId()).isEqualTo(7L);
        assertThat(r.getGoalId()).isEqualTo(33L);
        assertThat(r.getDate()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(r.getTrackingModel()).isEqualTo(TrackingModel.WEEKLY_COUNT);
        assertThat(r.getTargetCount()).isEqualTo(2);
        assertThat(r.getPeriod()).isEqualTo("WEEKLY");
    }

    @Test
    void skipsDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        when(repo.existsByEventId(eventId)).thenReturn(true);

        listener.onProgressCreated(created(eventId));

        verify(repo, never()).save(any());
    }

    @Test
    void deletesRecordOnDeletedEvent() {
        listener.onProgressDeleted(new ProgressDeletedEvent(UUID.randomUUID(), 5L, 7L, 33L, Instant.now()));

        verify(repo).deleteByProgressId(5L);
    }
}
