package com.example.moodtail.domain.term.repository;

import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByActiveTrueOrderByIdAsc();

    List<Term> findByTermTypeAndActiveTrueOrderByIdAsc(TermType termType);
}
