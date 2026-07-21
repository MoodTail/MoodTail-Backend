package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.SharedMoodTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SharedMoodTestResultRepository extends JpaRepository<SharedMoodTestResult, Long> {

    Optional<SharedMoodTestResult> findByShareToken(String shareToken);

    @Query("""
            select result.thumbnailImageUrl
              from SharedMoodTestResult result
             where result.user.id = :userId
               and result.thumbnailImageUrl is not null
            """)
    List<String> findThumbnailImageUrlsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from SharedMoodTestResult result where result.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
