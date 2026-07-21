package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MoodTestResultRepository extends JpaRepository<MoodTestResult, Long> {

    Optional<MoodTestResult> findByUserIdAndResultDate(Long userId, LocalDate resultDate);

    Optional<MoodTestResult> findByShareToken(String shareToken);

    @Modifying(flushAutomatically = true)
    @Query("delete from MoodTestResult result where result.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
