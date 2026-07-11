package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.LocalAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocalAccountRepository extends JpaRepository<LocalAccount, Long> {

    Optional<LocalAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from LocalAccount account join fetch account.user where account.email = :email")
    Optional<LocalAccount> findByEmailForUpdate(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from LocalAccount account join fetch account.user where account.id = :accountId")
    Optional<LocalAccount> findByIdForUpdate(@Param("accountId") Long accountId);
}
