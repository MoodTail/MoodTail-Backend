package com.example.moodtail.domain.auth.repository;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.global.auth.model.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );
}
