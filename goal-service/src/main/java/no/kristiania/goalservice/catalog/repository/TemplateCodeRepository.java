package no.kristiania.goalservice.catalog.repository;

import no.kristiania.goalservice.catalog.entity.GoalTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TemplateCodeRepository extends Repository<GoalTemplate, Long> {

    /** Rows of [templateId, templateCode, directionCode]. */
    @Query(value = """
            SELECT t.id, t.code, d.code
            FROM goal_templates t
            JOIN directions d ON d.id = t.direction_id
            WHERE t.id IN (:ids)
            """, nativeQuery = true)
    List<Object[]> findCodes(@Param("ids") Collection<Long> ids);
}
