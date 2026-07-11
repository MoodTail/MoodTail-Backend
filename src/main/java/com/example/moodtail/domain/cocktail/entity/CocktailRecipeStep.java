package com.example.moodtail.domain.cocktail.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cocktail_recipe_steps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CocktailRecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}