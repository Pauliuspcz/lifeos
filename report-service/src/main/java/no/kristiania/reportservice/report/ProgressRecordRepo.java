package no.kristiania.reportservice.report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressRecordRepo extends JpaRepository<ProgressRecord, Long> {
    boolean existsByEventId(UUID eventId);
    List<ProgressRecord> findByUserIdAndGoalIdOrderByDateAsc(Long userId, Long goalId);
}
