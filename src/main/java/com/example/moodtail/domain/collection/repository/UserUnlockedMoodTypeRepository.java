package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserUnlockedMoodTypeRepository extends JpaRepository<UserUnlockedMoodType, Long> {

    boolean existsByUserIdAndMoodTypeId(Long userId, Long moodTypeId);

    @EntityGraph(attributePaths = {"moodType", "moodType.characterImage"})
    Optional<UserUnlockedMoodType> findByUserIdAndMoodTypeId(Long userId, Long moodTypeId);

    @Query("""
            select moodType.id as moodTypeId,
                   moodType.code as typeCode,
                   moodType.name as name,
                   moodType.shortDescription as shortDescription,
                   characterImage.imageUrl as characterImageUrl,
                   unlockedMoodType.unlockedAt as unlockedAt
              from MoodType moodType
              left join moodType.characterImage characterImage
              left join UserUnlockedMoodType unlockedMoodType
                on unlockedMoodType.moodType = moodType
               and unlockedMoodType.user.id = :userId
             order by moodType.sortOrder asc, moodType.id asc
            """)
    List<MoodTypeCollectionProjection> findAllMoodTypesByUserId(@Param("userId") Long userId);

    @Query("""
            select unlocked.moodType.id
              from UserUnlockedMoodType unlocked
             where unlocked.user.id = :userId
            """)
    List<Long> findMoodTypeIdsByUserId(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from UserUnlockedMoodType unlocked
             where unlocked.user.id = :guestUserId
               and unlocked.moodType.id in :moodTypeIds
            """)
    int deleteAllByUserIdAndMoodTypeIdIn(
            @Param("guestUserId") Long guestUserId,
            @Param("moodTypeIds") List<Long> moodTypeIds
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            update UserUnlockedMoodType unlocked
               set unlocked.user = :targetUser
             where unlocked.user.id = :guestUserId
            """)
    int transferAllByUserId(
            @Param("guestUserId") Long guestUserId,
            @Param("targetUser") User targetUser
    );

    @Modifying(flushAutomatically = true)
    @Query("delete from UserUnlockedMoodType unlocked where unlocked.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
