package com.example.moodtail.domain.auth.repository;

import com.example.moodtail.domain.history.entity.DrinkingRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 회원탈퇴 트랜잭션에서 사용자의 음주 기록과 사진 연결만 정리한다.
 */
public interface WithdrawalHistoryRepository extends Repository<DrinkingRecord, Long> {

    @Query(value = """
            select distinct photo.image_id
              from history_photos photo
             where photo.user_id = :userId
            """, nativeQuery = true)
    List<Long> findOwnedImageIdsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query(value = "delete from history_photos where user_id = :userId", nativeQuery = true)
    int deletePhotosByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("delete from DrinkingRecord record where record.user.id = :userId")
    int deleteRecordsByUserId(@Param("userId") Long userId);

}
