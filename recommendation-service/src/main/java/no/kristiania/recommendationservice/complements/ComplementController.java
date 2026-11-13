package no.kristiania.recommendationservice.complements;

import jakarta.validation.Valid;
import no.kristiania.recommendationservice.complements.dto.ComplementRequest;
import no.kristiania.recommendationservice.complements.dto.ComplementSuggestion;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class ComplementController {
    private final ComplementService complementService;

    public ComplementController(ComplementService complementService) {
        this.complementService = complementService;
    }

    @PostMapping("/goals")
    public List<ComplementSuggestion> getComplements(@Valid @RequestBody ComplementRequest request) {
        return complementService.getSuggestions(request.categories());
    }
}
