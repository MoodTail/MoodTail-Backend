package com.example.moodtail.domain.moodtest.repository;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    @EntityGraph(attributePaths = {"moodType", "image"})
    List<Cocktail> findByMoodTypeId(Long moodTypeId);
}
