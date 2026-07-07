package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoodTypeCompatibilityRepository extends JpaRepository<MoodTypeCompatibility, Long> {

    @EntityGraph(attributePaths = "targetMoodType")
    Optional<MoodTypeCompatibility> findByMoodTypeIdAndCompatibilityType(Long moodTypeId, CompatibilityType compatibilityType);
}
