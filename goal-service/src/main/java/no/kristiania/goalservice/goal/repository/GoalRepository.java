package no.kristiania.goalservice.goal.repository;

import no.kristiania.goalservice.goal.entity.Goal;
import no.kristiania.goalservice.goal.entity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findAllByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);
}