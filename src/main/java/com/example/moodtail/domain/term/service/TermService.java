package com.example.moodtail.domain.term.service;

import com.example.moodtail.domain.term.dto.response.TermsResponse;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.TermErrorStatus.ACTIVE_TERM_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.TermErrorStatus.INVALID_TERM_TYPE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermRepository termRepository;

    public TermsResponse getTerms(String termType) {
        List<Term> terms = termType == null
                ? termRepository.findByActiveTrueOrderByIdAsc()
                : termRepository.findByTermTypeAndActiveTrueOrderByIdAsc(parseTermType(termType));

        if (terms.isEmpty()) {
            throw new RestApiException(ACTIVE_TERM_NOT_FOUND);
        }
        return TermsResponse.from(terms);
    }

    private TermType parseTermType(String termType) {
        if (termType.isBlank()) {
            throw new RestApiException(INVALID_TERM_TYPE);
        }

        try {
            return TermType.valueOf(termType);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(INVALID_TERM_TYPE);
        }
    }
}
