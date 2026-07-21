package com.example.moodtail.domain.inquiry.repository;

import com.example.moodtail.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            update Inquiry inquiry
               set inquiry.user = null,
                   inquiry.contactEmail = null
             where inquiry.user.id = :userId
            """)
    int anonymizeAllByUserId(@Param("userId") Long userId);
}
