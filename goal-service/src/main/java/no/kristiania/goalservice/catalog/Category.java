package no.kristiania.goalservice.catalog;

import jakarta.persistence.*;
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