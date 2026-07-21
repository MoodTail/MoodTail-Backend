package com.example.moodtail.domain.cocktail.repository;

import com.example.moodtail.domain.history.entity.DrinkingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrinkingRecordRepository extends JpaRepository<DrinkingRecord, Long> {
    long countByUser_Id(Long userId);
    long countByUser_IdAndCocktail_MoodType_Id(Long userId, Long moodTypeId);
}
