package no.kristiania.goalservice.goal.service;

import no.kristiania.goalservice.catalog.repository.TemplateCodeRepository;
import no.kristiania.goalservice.goal.entity.Goal;
import no.kristiania.goalservice.goal.dto.GoalResponse;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GoalResponseMapper {
    private final TemplateCodeRepository templateCodes;

    public GoalResponseMapper(TemplateCodeRepository templateCodes) {
        this.templateCodes = templateCodes;
    }

    public GoalResponse toResponse(Goal goal) {
        return toResponses(List.of(goal)).get(0);
    }

    public List<GoalResponse> toResponses(List<Goal> goals) {
        Set<Long> templateIds = goals.stream()
                .map(Goal::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String[]> codesById = new HashMap<>();
        if (!templateIds.isEmpty()) {
            for (Object[] row : templateCodes.findCodes(templateIds)) {
                codesById.put(((Number) row[0]).longValue(), new String[]{(String) row[1], (String) row[2]});
            }
        }

        return goals.stream().map(goal -> {
            String[] codes = goal.getTemplateId() == null ? null : codesById.get(goal.getTemplateId());
            return GoalResponse.from(goal, codes == null ? null : codes[0], codes == null ? null : codes[1]);
        }).toList();
    }
}
