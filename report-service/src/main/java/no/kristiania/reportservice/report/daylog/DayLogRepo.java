package no.kristiania.reportservice.report.daylog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayLogRepo extends JpaRepository<DayLog, Long> {
    Optional<DayLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<DayLog> findByUserIdAndDateGreaterThanEqual(Long userId, LocalDate from);
}