package no.kristiania.reportservice.daylog.repository;

import no.kristiania.reportservice.daylog.entity.DayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayLogRepository extends JpaRepository<DayLog, Long> {
    Optional<DayLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<DayLog> findByUserIdAndDateGreaterThanEqual(Long userId, LocalDate from);
}