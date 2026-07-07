package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.SocialAccount;
import com.example.moodtail.domain.user.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, SocialProvider provider);
}
