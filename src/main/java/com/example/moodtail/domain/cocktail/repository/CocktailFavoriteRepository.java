package com.example.moodtail.domain.cocktail.repository;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.CocktailFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CocktailFavoriteRepository extends JpaRepository<CocktailFavorite, Long> {

    @Query("""
            SELECT favorite.cocktail.id
            FROM CocktailFavorite favorite
            WHERE favorite.user.id = :userId
              AND favorite.cocktail.id IN :cocktailIds
            """)
    List<Long> findFavoriteCocktailIds(
            @Param("userId") Long userId,
            @Param("cocktailIds") Collection<Long> cocktailIds
    );

    @Query("""
            SELECT favorite.cocktail
            FROM CocktailFavorite favorite
            LEFT JOIN FETCH favorite.cocktail.image
            WHERE favorite.user.id = :userId
            ORDER BY favorite.id DESC
            """)
    List<Cocktail> findFavoriteCocktailsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT favorite
            FROM CocktailFavorite favorite
            WHERE favorite.user.id = :userId
              AND favorite.cocktail.id = :cocktailId
            """)
    Optional<CocktailFavorite> findByUserIdAndCocktailId(
            @Param("userId") Long userId,
            @Param("cocktailId") Long cocktailId
    );

    @Query("""
            SELECT CASE WHEN COUNT(favorite) > 0 THEN true ELSE false END
            FROM CocktailFavorite favorite
            WHERE favorite.user.id = :userId
              AND favorite.cocktail.id = :cocktailId
            """)
    boolean existsByUserIdAndCocktailId(
            @Param("userId") Long userId,
            @Param("cocktailId") Long cocktailId
    );
}
