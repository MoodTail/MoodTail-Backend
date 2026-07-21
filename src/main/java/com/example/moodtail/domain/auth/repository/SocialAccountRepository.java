package com.example.moodtail.domain.auth.repository;

import com.example.moodtail.domain.auth.entity.SocialAccount;
import com.example.moodtail.global.auth.model.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    @Modifying(flushAutomatically = true)
    @Query("delete from SocialAccount account where account.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
