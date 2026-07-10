package com.example.moodtail.domain.user.repository;

import com.example.moodtail.domain.user.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findAllByActiveTrue();
}
