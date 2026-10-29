package no.kristiania.goalservice.catalog.dto;

import no.kristiania.goalservice.catalog.Direction;

public record DirectionResponse(Long id, String code, String name, String description,
                                String iconKey, String color, int sortOrder) {
    public static DirectionResponse from(Direction d) {
        return new DirectionResponse(d.getId(), d.getCode(), d.getName(), d.getDescription(),
                d.getIconKey(), d.getColor(), d.getSortOrder());
    }
}
