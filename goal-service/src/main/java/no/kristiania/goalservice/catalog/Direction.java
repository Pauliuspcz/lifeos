package no.kristiania.goalservice.catalog;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Immutable
@Table(name = "directions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Direction {

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

    @Immutable
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "direction_templates",
            joinColumns = @JoinColumn(name = "direction_id"),
            inverseJoinColumns = @JoinColumn(name = "template_id"))
    private Set<GoalTemplate> templates = new LinkedHashSet<>();
}