package com.example.moodtail.domain.cocktail.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cocktail_ingredients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CocktailIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(nullable = false)
    private String name;

    @Column(name = "amount_text")
    private String amountText;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}