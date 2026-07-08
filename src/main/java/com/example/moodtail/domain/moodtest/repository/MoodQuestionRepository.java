package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodQuestion;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodQuestionRepository extends JpaRepository<MoodQuestion, Long> {

    @EntityGraph(attributePaths = "options")
    List<MoodQuestion> findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType questionType);
}
