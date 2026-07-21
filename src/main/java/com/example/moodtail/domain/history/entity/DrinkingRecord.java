package com.example.moodtail.domain.history.entity;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "drinking_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_drinking_record_user_date",
                columnNames = {"user_id", "record_date"}
        )
)
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

    private DrinkingRecord(
            User user,
            Cocktail cocktail,
            LocalDate recordDate,
            LocalDateTime recordedAt
    ) {
        this.user = user;
        this.cocktail = cocktail;
        this.recordDate = recordDate;
        this.recordedAt = recordedAt;
    }

    public static DrinkingRecord create(
            User user,
            Cocktail cocktail,
            LocalDate recordDate,
            LocalDateTime recordedAt
    ) {
        return new DrinkingRecord(user, cocktail, recordDate, recordedAt);
    }

    public void updateRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public void updateCocktail(Cocktail cocktail) {
        this.cocktail = cocktail;
    }
}
