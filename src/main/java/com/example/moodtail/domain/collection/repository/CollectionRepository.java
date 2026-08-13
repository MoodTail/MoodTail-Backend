package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRepository extends Repository<MoodType, Long> {
    @Query("""
              select moodType.id as moodTypeId,
                     moodType.code as typeCode,
                     moodType.name as name,
                     characterImage.imageUrl as characterImageUrl,
                     userUnlockedMoodType.id as unlockedMoodTypeId,
                     userUnlockedMoodType.unlockedAt as unlockedAt,
                     count(distinct cocktail.id) as totalCocktailCount,
                     count(distinct userUnlockedCocktail.id) as collectedUserCount
                from MoodType moodType
                left join moodType.characterImage characterImage
                left join UserUnlockedMoodType userUnlockedMoodType
                  on userUnlockedMoodType.moodType = moodType
                 and userUnlockedMoodType.user.id = :userId
                left join Cocktail cocktail
                  on cocktail.moodType = moodType
                left join UserUnlockedCocktail userUnlockedCocktail
                  on userUnlockedCocktail.cocktail = cocktail
                 and userUnlockedCocktail.user.id = :userId
               group by moodType.id,
                        moodType.code,
                        moodType.name,
                        characterImage.imageUrl,
                        userUnlockedMoodType.id,
                        userUnlockedMoodType.unlockedAt,
                        moodType.sortOrder
               order by moodType.sortOrder asc,
                        moodType.id asc
              """)
    List<CollectionProjection> findCollectionByUserId(
            @Param("userId") Long userId
    );
}
