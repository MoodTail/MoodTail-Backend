package com.example.moodtail.domain.image.repository;

import com.example.moodtail.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("""
            SELECT image
            FROM Image image
            WHERE image.id IN :imageIds
              AND NOT EXISTS (
                  SELECT photo.id
                  FROM HistoryPhoto photo
                  WHERE photo.image.id = image.id
              )
              AND NOT EXISTS (
                  SELECT cocktail.id
                  FROM Cocktail cocktail
                  WHERE cocktail.image.id = image.id
              )
              AND NOT EXISTS (
                  SELECT moodType.id
                  FROM MoodType moodType
                  WHERE moodType.characterImage.id = image.id
              )
            """)
    List<Image> findUnreferencedByIdIn(@Param("imageIds") List<Long> imageIds);
}
