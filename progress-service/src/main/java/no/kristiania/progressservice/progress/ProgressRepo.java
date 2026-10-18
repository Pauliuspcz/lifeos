package no.kristiania.progressservice.progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepo extends JpaRepository<Progress,Long> {
    List<Progress> findByGoalIdAndUserIdOrderByDateDesc(Long goalId, Long userId);
    List<Progress> findByUserIdOrderByDateDesc(Long userId);
    Optional<Progress> findByIdAndUserId(Long id, Long userId);
}

