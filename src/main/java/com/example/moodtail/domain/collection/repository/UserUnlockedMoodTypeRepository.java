package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserUnlockedMoodTypeRepository extends JpaRepository<UserUnlockedMoodType, Long> {

    @EntityGraph(attributePaths = {"moodType", "moodType.characterImage"})
    Optional<UserUnlockedMoodType> findByUserIdAndMoodTypeId(Long userId, Long moodTypeId);

    @EntityGraph(attributePaths = {"moodType", "moodType.characterImage"})
    @Query("""
            select unlockedMoodType
              from UserUnlockedMoodType unlockedMoodType
             where unlockedMoodType.user.id = :userId
             order by unlockedMoodType.moodType.sortOrder asc,
                      unlockedMoodType.moodType.id asc
            """)
    List<UserUnlockedMoodType> findAllByUserId(@Param("userId") Long userId);
}
