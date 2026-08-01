package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cocktail_trend_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CocktailTrendSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 트렌드 집계 결과(CocktailTrendResponse) 전체를 JSON으로 직렬화해서 저장
    @Convert(converter = CocktailTrendSnapshotConverter.class)
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "json")
    private CocktailTrendResponse snapshotData;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private CocktailTrendSnapshot(CocktailTrendResponse snapshotData, LocalDateTime updatedAt) {
        this.snapshotData = snapshotData;
        this.updatedAt = updatedAt;
    }

    public static CocktailTrendSnapshot create(CocktailTrendResponse snapshotData, LocalDateTime updatedAt) {
        return new CocktailTrendSnapshot(snapshotData, updatedAt);
    }

    // 스케줄러가 재계산한 최신 집계 결과로 갱신
    public void updateSnapshot(CocktailTrendResponse snapshotData, LocalDateTime updatedAt) {
        this.snapshotData = snapshotData;
        this.updatedAt = updatedAt;
    }
}
