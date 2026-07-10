package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MoodTestResultRepository extends JpaRepository<MoodTestResult, Long> {

    Optional<MoodTestResult> findByUserIdAndResultDate(Long userId, LocalDate resultDate);
}
