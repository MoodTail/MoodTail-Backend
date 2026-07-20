package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MoodTypeRepository extends JpaRepository<MoodType, Long> {

    @EntityGraph(attributePaths = "characterImage")
    List<MoodType> findAllByOrderBySortOrderAscIdAsc();

    @EntityGraph(attributePaths = "characterImage")
    @Query("""
          select moodType
            from MoodType moodType
           where moodType.id = :moodTypeId
          """)
    Optional<MoodType> findDetailById(
            @Param("moodTypeId") Long moodTypeId
    );
}
