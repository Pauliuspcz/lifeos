package no.kristiania.recommendationservice.outdoor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutdoorGoalRepo extends JpaRepository<OutdoorGoal, Long> {
}
