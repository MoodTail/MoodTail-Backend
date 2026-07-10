package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"representativeMoodType", "representativeMoodType.characterImage"})
    Optional<User> findById(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query(value = """
            SELECT u.user_id AS userId,
                   u.nickname AS nickname,
                   mt.id AS moodTypeId,
                   mt.code AS moodTypeCode,
                   mt.name AS moodTypeName,
                   img.image_url AS characterImageUrl,
                   (SELECT COUNT(*)
                      FROM mood_test_results mtr
                     WHERE mtr.user_id = u.user_id) AS totalTestCount,
                   (SELECT COUNT(*)
                      FROM drinking_records dr
                     WHERE dr.user_id = u.user_id
                       AND dr.record_date >= :monthStart
                       AND dr.record_date < :nextMonthStart) AS monthlyRecordCount,
                   (SELECT COUNT(*)
                      FROM user_unlocked_mood_types umt
                     WHERE umt.user_id = u.user_id) AS unlockedMoodTypeCount
              FROM users u
              LEFT JOIN mood_types mt ON mt.id = u.representative_mood_type_id
              LEFT JOIN images img ON img.id = mt.character_image_id
             WHERE u.user_id = :userId
            """, nativeQuery = true)
    Optional<MyPageProjection> findMyPageByUserId(
            @Param("userId") Long userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("nextMonthStart") LocalDate nextMonthStart
    );
}
