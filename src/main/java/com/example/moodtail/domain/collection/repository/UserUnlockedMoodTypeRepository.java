package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserUnlockedMoodTypeRepository extends JpaRepository<UserUnlockedMoodType, Long> {

    @EntityGraph(attributePaths = {"moodType", "moodType.characterImage"})
    Optional<UserUnlockedMoodType> findByUserIdAndMoodTypeId(Long userId, Long moodTypeId);
}
