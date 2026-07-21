package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoodTypeCompatibilityRepository extends JpaRepository<MoodTypeCompatibility, Long> {

    @EntityGraph(attributePaths = {"targetMoodType", "targetMoodType.characterImage"})
    Optional<MoodTypeCompatibility> findByMoodTypeIdAndCompatibilityType(Long moodTypeId, CompatibilityType compatibilityType);

    @EntityGraph(attributePaths = {
            "targetMoodType",
            "targetMoodType.characterImage"
    })
    List<MoodTypeCompatibility> findAllByMoodTypeId(
            Long moodTypeId
    );
}
