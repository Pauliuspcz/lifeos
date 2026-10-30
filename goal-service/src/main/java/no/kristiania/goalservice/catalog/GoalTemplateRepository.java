package no.kristiania.goalservice.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalTemplateRepository extends JpaRepository<GoalTemplate, Long> {

    @Query("""
            select t from Direction d join d.templates t
            where d.id = :directionId and t.active = true
            order by t.category.sortOrder, t.sortOrder""")
    List<GoalTemplate> findActiveByDirectionId(@Param("directionId") Long directionId);

    List<GoalTemplate> findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(Long categoryId);
}
