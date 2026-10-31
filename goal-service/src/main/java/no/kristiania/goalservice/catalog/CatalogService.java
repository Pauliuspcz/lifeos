package no.kristiania.goalservice.catalog;

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

    public List<GoalTemplate> listTemplatesForDirection(Long directionId) {
        if (!directions.existsById(directionId)) {
            throw new CatalogItemNotFoundException("Direction", directionId);
        }
        return templates.findActiveByDirectionId(directionId);
    }

    public List<GoalTemplate> listTemplatesForCategory(Long categoryId) {
        if (!categories.existsById(categoryId)) {
            throw new CatalogItemNotFoundException("Category", categoryId);
        }
        return templates.findAllByCategoryIdAndActiveTrueOrderBySortOrderAsc(categoryId);
    }
}
