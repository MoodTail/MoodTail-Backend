package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.entity.UserStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGuestUuidAndRole(String guestUuid, UserRole role);

    Optional<User> findByInviteCode(String inviteCode);

    @Override
    @EntityGraph(attributePaths = {"representativeMoodType", "representativeMoodType.characterImage"})
    Optional<User> findById(Long userId);

    long countByStatusAndDeletedAtIsNull(UserStatus status);
    long countByRepresentativeMoodType_IdAndStatusAndDeletedAtIsNull(
            Long moodTypeId,
            UserStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query(value = """
            SELECT u.id AS userId,
                   u.nickname AS nickname,
                   mt.id AS moodTypeId,
                   mt.code AS moodTypeCode,
                   mt.name AS moodTypeName,
                   img.image_url AS characterImageUrl,
                   (SELECT COUNT(*)
                      FROM mood_test_results mtr
                     WHERE mtr.user_id = u.id) AS totalTestCount,
                   (SELECT COUNT(*)
                      FROM drinking_records dr
                     WHERE dr.user_id = u.id
                       AND dr.record_date >= :monthStart
                       AND dr.record_date < :nextMonthStart) AS monthlyRecordCount,
                   (SELECT COUNT(*)
                      FROM user_unlocked_mood_types umt
                     WHERE umt.user_id = u.id) AS unlockedMoodTypeCount
              FROM users u
              LEFT JOIN mood_types mt ON mt.id = u.representative_mood_type_id
              LEFT JOIN images img ON img.id = mt.character_image_id
             WHERE u.id = :userId
            """, nativeQuery = true)
    Optional<MyPageProjection> findMyPageByUserId(
            @Param("userId") Long userId,
            @Param("monthStart") LocalDate monthStart,
            @Param("nextMonthStart") LocalDate nextMonthStart
    );
}
