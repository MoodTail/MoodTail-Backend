package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodTypeRepository extends JpaRepository<MoodType, Long> {

    List<MoodType> findAllByOrderBySortOrderAscIdAsc();
}
