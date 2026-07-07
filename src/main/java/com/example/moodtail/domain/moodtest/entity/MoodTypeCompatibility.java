package com.example.moodtail.domain.moodtest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "mood_type_compatibilities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodTypeCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_type_id", nullable = false)
    private MoodType moodType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_mood_type_id", nullable = false)
    private MoodType targetMoodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "compatibility_type", nullable = false, length = 20)
    private CompatibilityType compatibilityType;
}
