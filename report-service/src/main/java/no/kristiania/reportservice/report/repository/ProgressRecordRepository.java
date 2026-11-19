package no.kristiania.reportservice.report.repository;
import no.kristiania.reportservice.report.entity.ProgressRecord;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProgressRecordRepository extends JpaRepository<ProgressRecord, Long> {
    boolean existsByEventId(UUID eventId);
    List<ProgressRecord> findByUserIdAndGoalIdOrderByDateAsc(Long userId, Long goalId);
    List<ProgressRecord> findByUserIdAndDateGreaterThanEqual(Long userId, LocalDate from);
    @Transactional
    void deleteByProgressId(Long progressId);
}
