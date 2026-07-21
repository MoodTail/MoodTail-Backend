package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.UserTermAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTermAgreementRepository extends JpaRepository<UserTermAgreement, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from UserTermAgreement agreement where agreement.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
