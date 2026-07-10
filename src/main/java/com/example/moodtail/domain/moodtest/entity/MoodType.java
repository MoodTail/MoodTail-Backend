package com.example.moodtail.domain.moodtest.entity;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "mood_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "character_quote")
    private String characterQuote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_image_id")
    private Image characterImage;

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

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public TasteProfile toTasteProfile() {
        return TasteProfile.of(alcoholIntensity, sweetness, sourness, refreshing, bitterness);
    }
}
