package no.kristiania.goalservice.catalog.repository;

import no.kristiania.goalservice.catalog.entity.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
    List<Direction> findAllByOrderBySortOrderAsc();
    List<Direction> findAllByCategoryIdOrderBySortOrderAsc(Long categoryId);
}