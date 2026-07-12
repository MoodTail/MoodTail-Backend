package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.domain.cocktail.enums.BaseSpirit;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cocktails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cocktail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_type_id", nullable = false)
    private MoodType moodType;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Column(name = "name_en", nullable = false, unique = true, length = 100)
    private String nameEn;

    @Column(name = "short_description")
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name="base_spirit")
    private BaseSpirit baseSpirit;

    @Column(name = "alcohol_degree", precision = 4, scale = 1)
    private BigDecimal alcoholDegree;

    @Column(name = "alcohol_intensity", nullable = false, precision = 2, scale = 1)
    private BigDecimal alcoholIntensity;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal sweetness;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal sourness;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal refreshing;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal bitterness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "pairing_snack")
    private String pairingSnack;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "cocktail", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<CocktailIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "cocktail", fetch = FetchType.LAZY)
    @OrderBy("stepOrder ASC")
    private List<CocktailRecipeStep> recipeSteps = new ArrayList<>();

    public TasteProfile toTasteProfile() {
        return TasteProfile.of(alcoholIntensity, sweetness, sourness, refreshing, bitterness);
    }
}
