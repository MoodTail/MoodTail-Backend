package com.example.moodtail.domain.cocktail.repository;

import com.example.moodtail.domain.cocktail.entity.CocktailTrendSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CocktailTrendSnapshotRepository extends JpaRepository<CocktailTrendSnapshot, Long> {

    // 스냅샷은 항상 최신 1건만 유지 - 저장/갱신은 JpaRepository 기본 save()를 사용
    Optional<CocktailTrendSnapshot> findTopByOrderByIdDesc();
}
