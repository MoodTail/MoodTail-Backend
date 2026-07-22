package com.example.moodtail.domain.collection.repository;

import com.example.moodtail.domain.collection.entity.CollectionShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollectionShareRepository extends JpaRepository<CollectionShare, Long> {
    Optional<CollectionShare> findByUserId(Long userId);

    Optional<CollectionShare> findByShareToken(String shareToken);

    @Query("""
              select share.thumbnailImageUrl
                from CollectionShare share
               where share.user.id = :userId
              """)
    Optional<String> findThumbnailImageUrlByUserId(
            @Param("userId") Long userId
    );

    @Modifying(flushAutomatically = true)
    @Query("""
              delete from CollectionShare share
               where share.user.id = :userId
              """)
    int deleteByUserId(@Param("userId") Long userId);
}
