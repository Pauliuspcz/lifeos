package no.kristiania.recommendationservice.relations;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelationService {
    private final RelationProperties relationProperties;

    public RelationService(RelationProperties relationProperties) {
        this.relationProperties = relationProperties;
    }

    public List<RelationHint> evaluate(RelationRequest request) {
        return RelationFinder.find(request.goals(), request.candidate(), relationProperties);
    }
}
