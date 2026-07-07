package com.example.moodtail.domain.moodtest.entity;

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
@Table(name = "mood_question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_question_id", nullable = false)
    private MoodQuestion moodQuestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "alcohol_intensity_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal alcoholIntensityScore;

    @Column(name = "sweetness_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal sweetnessScore;

    @Column(name = "sourness_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal sournessScore;

    @Column(name = "refreshing_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal refreshingScore;

    @Column(name = "bitterness_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal bitternessScore;
}
