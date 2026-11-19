package no.kristiania.progressservice.progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepo extends JpaRepository<Progress,Long> {
    List<Progress> findByGoalIdAndUserIdOrderByDateDesc(Long goalId, Long userId);
    List<Progress> findByUserIdOrderByDateDesc(Long userId);
    Optional<Progress> findByIdAndUserId(Long id, Long userId);
    List<Progress> findByUserIdAndDateBetweenOrderByDateDesc(Long userId, LocalDate from, LocalDate to);
    Optional<Progress> findByUserIdAndGoalIdAndDate(Long userId, Long goalId, LocalDate date);
    long countByUserIdAndGoalIdAndDateBetween(Long userId, Long goalId, LocalDate from, LocalDate to);
    @Query("select coalesce(sum(p.amount), 0) from Progress p " +
            "where p.userId = :userId and p.goalId = :goalId")
    BigDecimal sumAmount(@Param("userId") Long userId, @Param("goalId") Long goalId);
    @Query("select coalesce(sum(p.amount), 0) from Progress p " +
            "where p.userId = :userId and p.goalId = :goalId " +
            "and p.date between :from and :to")
    BigDecimal sumAmountBetween(@Param("userId") Long userId, @Param("goalId") Long goalId,
                                @Param("from") LocalDate from, @Param("to") LocalDate to);
}

