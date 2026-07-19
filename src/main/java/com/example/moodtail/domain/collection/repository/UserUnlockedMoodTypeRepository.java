package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    boolean existsByUserIdAndMoodTypeId(
            Long userId,
            Long moodTypeId
    );
}
