package no.kristiania.recommendationservice.unit.outdoor;

import no.kristiania.recommendationservice.outdoor.OutdoorGoal;
import no.kristiania.recommendationservice.outdoor.OutdoorGoalListener;
import no.kristiania.recommendationservice.outdoor.OutdoorGoalRepo;
import no.kristiania.recommendationservice.outdoor.events.GoalOutdoorChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutdoorGoalListenerTest {
    @Mock
    OutdoorGoalRepo repo;
    @InjectMocks
    OutdoorGoalListener listener;

    private static GoalOutdoorChangedEvent event(boolean outdoor) {
        return new GoalOutdoorChangedEvent(UUID.randomUUID(), 33L, 7L, outdoor, Instant.now());
    }

    @Test
    void savesOutdoorGoalOnTrueEvent() {
        listener.onOutdoorChanged(event(true));

        ArgumentCaptor<OutdoorGoal> saved = ArgumentCaptor.forClass(OutdoorGoal.class);
        verify(repo).save(saved.capture());
        assertThat(saved.getValue().getGoalId()).isEqualTo(33L);
        assertThat(saved.getValue().getUserId()).isEqualTo(7L);
    }

    @Test
    void deletesOutdoorGoalOnFalseEvent() {
        listener.onOutdoorChanged(event(false));

        verify(repo).deleteById(33L);
        verify(repo, never()).save(any());
    }
}
