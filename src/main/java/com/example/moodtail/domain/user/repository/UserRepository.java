package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndStatus(Long id, UserStatus status);
}
