package com.example.moodtail.domain.report.repository;

import com.example.moodtail.domain.report.entity.MonthlyReportShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MonthlyReportShareRepository extends JpaRepository<MonthlyReportShare, Long> {

    Optional<MonthlyReportShare> findByShareTokenAndCreatedAtAfter(
            String shareToken,
            LocalDateTime createdAfter
    );

    @Query("""
            select share.shareImageUrl
              from MonthlyReportShare share
             where share.user.id = :userId
            """)
    List<String> findShareImageUrlsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from MonthlyReportShare share where share.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
