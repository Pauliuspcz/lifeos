package no.kristiania.goalservice.catalog.repository;

import no.kristiania.goalservice.catalog.entity.GoalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalTemplateRepository extends JpaRepository<GoalTemplate, Long> {
    List<GoalTemplate> findAllByDirectionIdAndActiveTrueOrderBySortOrderAsc(Long directionId);
    List<GoalTemplate> findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(Long categoryId);
}