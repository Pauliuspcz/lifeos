package no.kristiania.goalservice.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
    List<Direction> findAllByOrderBySortOrderAsc();
}