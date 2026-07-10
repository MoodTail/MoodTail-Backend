package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.UserTermAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermAgreementRepository extends JpaRepository<UserTermAgreement, Long> {
}
