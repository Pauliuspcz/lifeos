package no.kristiania.goalservice.catalog;

import no.kristiania.goalservice.catalog.dto.CategoryResponse;
import no.kristiania.goalservice.catalog.dto.DirectionResponse;
import no.kristiania.goalservice.catalog.dto.TemplateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalogService.listCategories().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/categories/{id}/templates")
    public List<TemplateResponse> categoryTemplates(@PathVariable Long id) {
        return catalogService.listTemplatesForCategory(id).stream().map(TemplateResponse::from).toList();
    }

    @GetMapping("/directions")
    public List<DirectionResponse> directions() {
        return catalogService.listDirections().stream().map(DirectionResponse::from).toList();
    }

    @GetMapping("/directions/{id}/templates")
    public List<TemplateResponse> directionTemplates(@PathVariable Long id) {
        return catalogService.listTemplatesForDirection(id).stream().map(TemplateResponse::from).toList();
    }
}