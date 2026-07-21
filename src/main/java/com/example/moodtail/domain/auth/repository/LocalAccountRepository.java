package com.example.moodtail.domain.auth.repository;

import com.example.moodtail.domain.auth.entity.LocalAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocalAccountRepository extends JpaRepository<LocalAccount, Long> {

    @Query("select account from LocalAccount account join fetch account.user where account.email = :email")
    Optional<LocalAccount> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from LocalAccount account join fetch account.user where account.email = :email")
    Optional<LocalAccount> findByEmailForUpdate(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from LocalAccount account join fetch account.user where account.id = :accountId")
    Optional<LocalAccount> findByIdForUpdate(@Param("accountId") Long accountId);

    @Modifying(flushAutomatically = true)
    @Query("delete from LocalAccount account where account.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}
