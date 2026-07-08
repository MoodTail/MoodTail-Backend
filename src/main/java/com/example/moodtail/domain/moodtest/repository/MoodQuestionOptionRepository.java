package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodQuestionOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MoodQuestionOptionRepository extends JpaRepository<MoodQuestionOption, Long> {

    @EntityGraph(attributePaths = {"moodQuestion", "scores"})
    List<MoodQuestionOption> findDistinctByIdIn(Collection<Long> ids);
}
