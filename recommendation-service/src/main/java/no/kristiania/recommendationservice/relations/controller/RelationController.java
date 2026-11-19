package no.kristiania.recommendationservice.relations.controller;

import jakarta.validation.Valid;
import no.kristiania.recommendationservice.relations.dto.RelationHint;
import no.kristiania.recommendationservice.relations.dto.RelationRequest;
import no.kristiania.recommendationservice.relations.service.RelationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RelationController {
    private final RelationService relationService;

    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("/goals")
    public List<RelationHint> evaluate(@Valid @RequestBody RelationRequest request) {
        return relationService.evaluate(request);
    }
}
