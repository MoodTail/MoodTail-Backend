package com.example.moodtail.domain.history.repository;

import com.example.moodtail.domain.history.entity.DrinkingRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<DrinkingRecord, Long> {

    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    boolean existsByUserIdAndRecordDateAndIdNot(Long userId, LocalDate recordDate, Long recordId);

    @Query("""
            select record.recordDate
              from DrinkingRecord record
             where record.user.id = :userId
               and record.recordDate between :startDate and :endDate
             order by record.recordDate
            """)
    List<LocalDate> findRecordDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    long countByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            select cocktail.id as cocktailId,
                   cocktail.nameKo as nameKo,
                   cocktail.nameEn as nameEn,
                   cocktail.shortDescription as shortDescription,
                   image.imageUrl as imageUrl,
                   count(record.id) as recordCount
              from DrinkingRecord record
              join record.cocktail cocktail
              left join cocktail.image image
             where record.user.id = :userId
               and record.recordDate between :startDate and :endDate
             group by cocktail.id,
                      cocktail.nameKo,
                      cocktail.nameEn,
                      cocktail.shortDescription,
                      image.imageUrl
             order by count(record.id) desc, cocktail.nameKo asc, cocktail.id asc
            """)
    List<FrequentCocktail> findFrequentCocktails(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            select record
              from DrinkingRecord record
              join fetch record.cocktail cocktail
              left join fetch cocktail.image
             where record.user.id = :userId
               and record.recordDate = :recordDate
            """)
    Optional<DrinkingRecord> findWithDetailsByUserIdAndRecordDate(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );

    @Query("""
            select record
              from DrinkingRecord record
              join fetch record.cocktail cocktail
              left join fetch cocktail.image
             where record.id = :recordId
               and record.user.id = :userId
            """)
    Optional<DrinkingRecord> findWithDetailsByIdAndUserId(
            @Param("recordId") Long recordId,
            @Param("userId") Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select record
              from DrinkingRecord record
             where record.id = :recordId
               and record.user.id = :userId
            """)
    Optional<DrinkingRecord> findOwnedForUpdate(
            @Param("recordId") Long recordId,
            @Param("userId") Long userId
    );

    interface FrequentCocktail {
        Long getCocktailId();

        String getNameKo();

        String getNameEn();

        String getShortDescription();

        String getImageUrl();

        long getRecordCount();
    }
}
