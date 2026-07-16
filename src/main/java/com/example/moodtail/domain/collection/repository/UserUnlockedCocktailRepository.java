package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.UserUnlockedCocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserUnlockedCocktailRepository extends JpaRepository<UserUnlockedCocktail, Long> {

    @Query("""
          select unlockedCocktail.cocktail.id
            from UserUnlockedCocktail unlockedCocktail
           where unlockedCocktail.user.id = :userId
             and unlockedCocktail.cocktail.moodType.id = :moodTypeId
          """)
    Set<Long> findUnlockedCocktailIds(
            @Param("userId") Long userId,
            @Param("moodTypeId") Long moodTypeId
    );

}
