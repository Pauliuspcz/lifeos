package no.kristiania.recommendationservice.outdoor.repository;

import no.kristiania.recommendationservice.outdoor.entity.OutdoorGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutdoorGoalRepository extends JpaRepository<OutdoorGoal, Long> {
    boolean existsByUserId(Long userId);
}
