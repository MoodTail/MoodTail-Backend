package com.example.moodtail.domain.history.repository;

import com.example.moodtail.domain.history.entity.HistoryPhoto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryPhotoRepository extends JpaRepository<HistoryPhoto, Long> {

    boolean existsByImageId(Long imageId);

    @Query("""
            select count(photo)
              from HistoryPhoto photo
             where photo.user.id = :userId
               and photo.recordDate = :recordDate
            """)
    long countByUserIdAndRecordDate(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );

    @Query("""
            select photo.recordDate as recordDate,
                   count(photo.id) as photoCount
              from HistoryPhoto photo
             where photo.user.id = :userId
               and photo.recordDate between :startDate and :endDate
             group by photo.recordDate
            """)
    List<PhotoCountByDate> findPhotoCountsByUserIdAndRecordDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select photo
              from HistoryPhoto photo
              join fetch photo.image
             where photo.user.id = :userId
               and photo.recordDate = :recordDate
             order by photo.id
            """)
    List<HistoryPhoto> findAllByUserIdAndRecordDate(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select photo
              from HistoryPhoto photo
              join fetch photo.image
             where photo.id = :photoId
               and photo.user.id = :userId
               and photo.recordDate = :recordDate
            """)
    Optional<HistoryPhoto> findOwnedForUpdate(
            @Param("photoId") Long photoId,
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );

    interface PhotoCountByDate {
        LocalDate getRecordDate();

        long getPhotoCount();
    }
}
