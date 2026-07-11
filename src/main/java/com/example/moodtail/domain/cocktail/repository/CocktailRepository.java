package com.example.moodtail.domain.cocktail.repository;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    @EntityGraph(attributePaths = {"moodType", "image"})
    List<Cocktail> findByMoodTypeId(Long moodTypeId);

    @EntityGraph(attributePaths = "image")
    @Query("""
            SELECT cocktail
            FROM Cocktail cocktail
            WHERE (:minAlcoholDegree IS NULL OR cocktail.alcoholDegree >= :minAlcoholDegree)
              AND (:maxAlcoholDegree IS NULL OR cocktail.alcoholDegree <= :maxAlcoholDegree)
              AND (:keyword IS NULL
                   OR LOWER(cocktail.nameKo) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(cocktail.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY cocktail.nameKo ASC, cocktail.id ASC
            """)
    List<Cocktail> searchCocktails(
            @Param("minAlcoholDegree") BigDecimal minAlcoholDegree,
            @Param("maxAlcoholDegree") BigDecimal maxAlcoholDegree,
            @Param("keyword") String keyword
    );
}
