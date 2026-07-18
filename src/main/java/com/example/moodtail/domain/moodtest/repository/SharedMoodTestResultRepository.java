package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.SharedMoodTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedMoodTestResultRepository extends JpaRepository<SharedMoodTestResult, Long> {
}
