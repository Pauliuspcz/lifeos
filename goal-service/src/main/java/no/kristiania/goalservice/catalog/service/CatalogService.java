package no.kristiania.goalservice.catalog.service;

import no.kristiania.goalservice.catalog.entity.Category;
import no.kristiania.goalservice.catalog.entity.Direction;
import no.kristiania.goalservice.catalog.entity.GoalTemplate;
import no.kristiania.goalservice.catalog.repository.CategoryRepository;
import no.kristiania.goalservice.catalog.repository.DirectionRepository;
import no.kristiania.goalservice.catalog.repository.GoalTemplateRepository;
import no.kristiania.goalservice.exception.CatalogItemNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final CategoryRepository categories;
    private final DirectionRepository directions;
    private final GoalTemplateRepository templates;

    public CatalogService(CategoryRepository categories, DirectionRepository directions,
                          GoalTemplateRepository templates) {
        this.categories = categories;
        this.directions = directions;
        this.templates = templates;
    }

    public List<Category> listCategories() {
        return categories.findAllByOrderBySortOrderAsc();
    }

    public List<Direction> listDirections() {
        return directions.findAllByOrderBySortOrderAsc();
    }

    public List<Direction> listDirectionsForCategory(Long categoryId) {
        if (!categories.existsById(categoryId)) {
            throw new CatalogItemNotFoundException("Category", categoryId);
        }
        return directions.findAllByCategoryIdOrderBySortOrderAsc(categoryId);
    }

    public List<GoalTemplate> listTemplatesForDirection(Long directionId) {
        if (!directions.existsById(directionId)) {
            throw new CatalogItemNotFoundException("Direction", directionId);
        }
        return templates.findAllByDirectionIdAndActiveTrueOrderBySortOrderAsc(directionId);
    }

    public List<GoalTemplate> listTemplatesForCategory(Long categoryId) {
        if (!categories.existsById(categoryId)) {
            throw new CatalogItemNotFoundException("Category", categoryId);
        }
        return templates.findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(categoryId);
    }
}