package com.example.moodtail.domain.image.repository;

import com.example.moodtail.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from Image image where image.id = :imageId")
    int deleteByImageId(@Param("imageId") Long imageId);
}
