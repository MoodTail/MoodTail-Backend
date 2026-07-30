package com.example.moodtail.domain.history.repository;

import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryMoodTestResultRepository extends Repository<MoodTestResult, Long> {

    @Query("""
            select result
              from MoodTestResult result
              join fetch result.moodType moodType
              left join fetch moodType.characterImage
             where result.user.id = :userId
               and result.resultDate between :startDate and :endDate
             order by result.resultDate desc, result.id desc
            """)
    List<MoodTestResult> findAllWithMoodType(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select result
              from MoodTestResult result
              join fetch result.moodType moodType
              left join fetch moodType.characterImage
             where result.user.id = :userId
               and result.resultDate = :resultDate
            """)
    Optional<MoodTestResult> findWithMoodTypeByUserIdAndResultDate(
            @Param("userId") Long userId,
            @Param("resultDate") LocalDate resultDate
    );

    @Query("""
            select result
              from MoodTestResult result
              join fetch result.moodType moodType
              left join fetch moodType.characterImage
             where result.id = :resultId
               and result.user.id = :userId
            """)
    Optional<MoodTestResult> findDetailByIdAndUserId(
            @Param("resultId") Long resultId,
            @Param("userId") Long userId
    );
}
