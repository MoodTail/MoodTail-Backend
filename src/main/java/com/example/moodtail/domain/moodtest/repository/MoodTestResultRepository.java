package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MoodTestResultRepository extends JpaRepository<MoodTestResult, Long> {

    Optional<MoodTestResult> findByUserIdAndResultDate(Long userId, LocalDate resultDate);

    Optional<MoodTestResult> findByShareToken(String shareToken);

    Optional<MoodTestResult> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from MoodTestResult result where result.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    @Query("""
            select result.moodType.id as moodTypeId,
                   result.moodType.code as typeCode,
                   result.moodType.name as name,
                   count(result) as resultCount
              from MoodTestResult result
             where result.resultDate between :startDate and :endDate
             group by result.moodType.id, result.moodType.code, result.moodType.name
             order by count(result) desc, result.moodType.name asc, result.moodType.id asc
            """)
    List<MoodTypeTrendCount> countMoodTypesByResultDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select avg(result.alcoholIntensity) as alcoholIntensity,
                   avg(result.sweetness) as sweetness,
                   avg(result.sourness) as sourness,
                   avg(result.refreshing) as refreshing,
                   avg(result.bitterness) as bitterness
              from MoodTestResult result
            """)
    AverageTasteProfile averageTasteProfileCumulative();

    interface MoodTypeTrendCount {
        Long getMoodTypeId();

        String getTypeCode();

        String getName();

        long getResultCount();
    }

    interface AverageTasteProfile {
        BigDecimal getAlcoholIntensity();

        BigDecimal getSweetness();

        BigDecimal getSourness();

        BigDecimal getRefreshing();

        BigDecimal getBitterness();
    }
}
