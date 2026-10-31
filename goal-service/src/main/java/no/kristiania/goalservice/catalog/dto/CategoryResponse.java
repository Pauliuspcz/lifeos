package no.kristiania.goalservice.catalog.dto;

import no.kristiania.goalservice.catalog.Category;

public record CategoryResponse(Long id, String code, String name, String description,
                               String iconKey, String color, int sortOrder) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getDescription(),
                c.getIconKey(), c.getColor(), c.getSortOrder());
    }
}
