package no.kristiania.goalservice.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    private Long id;

    private String code;
    private String name;
    private String description;

    @Column(name = "icon_key")
    private String iconKey;

    private String color;

    @Column(name = "sort_order")
    private int sortOrder;
}