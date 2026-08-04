package com.example.moodtail.domain.cocktail.entity;

import jakarta.persistence.Column;
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

    // 트렌드 집계 결과(CocktailTrendResponse)를 JSON 문자열로 직렬화해서 저장 - 직렬화/역직렬화는 서비스 계층에서 처리
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "json")
    private String snapshotData;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private CocktailTrendSnapshot(String snapshotData, LocalDateTime updatedAt) {
        this.snapshotData = snapshotData;
        this.updatedAt = updatedAt;
    }

    public static CocktailTrendSnapshot create(String snapshotData, LocalDateTime updatedAt) {
        return new CocktailTrendSnapshot(snapshotData, updatedAt);
    }

    // 스케줄러가 재계산한 최신 집계 결과로 갱신
    public void updateSnapshot(String snapshotData, LocalDateTime updatedAt) {
        this.snapshotData = snapshotData;
        this.updatedAt = updatedAt;
    }
}
