package no.kristiania.recommendationservice.relations.service;

import no.kristiania.recommendationservice.relations.utils.RelationFinder;
import no.kristiania.recommendationservice.relations.dto.RelationHint;
import no.kristiania.recommendationservice.relations.props.RelationProperties;
import no.kristiania.recommendationservice.relations.dto.RelationRequest;
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
