package com.example.moodtail.domain.history.entity;

import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "drinking_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DrinkingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id", nullable = false)
    private Cocktail cocktail;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @Column
    private Integer satisfaction;

    @Column(length = 255)
    private String memo;

    @PrePersist
    void prePersist() {
        this.recordedAt = LocalDateTime.now();
    }
}
