package com.example.moodtail.domain.auth.service;

import com.example.moodtail.domain.auth.model.Consent;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserTermAgreement;
import com.example.moodtail.domain.user.repository.UserTermAgreementRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermAgreementService {

    private final TermRepository termRepository;
    private final UserTermAgreementRepository userTermAgreementRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordValidatedAgreements(User user, List<Term> agreedTerms, LocalDateTime agreedAt) {
        userTermAgreementRepository.saveAll(agreedTerms.stream()
                .map(term -> UserTermAgreement.create(user, term, agreedAt))
                .toList());
    }

    public List<Term> validateAgreements(List<Consent> consents) {
        if (consents == null || consents.isEmpty()) {
            throw new RestApiException(AuthErrorStatus.INVALID_TERM_AGREEMENT);
        }
        List<Term> activeTerms = termRepository.findByActiveTrueOrderByIdAsc();
        List<Term> requiredTerms = activeTerms.stream().filter(Term::isRequired).toList();
        long activeTermTypeCount = activeTerms.stream().map(Term::getTermType).distinct().count();
        if (requiredTerms.isEmpty() || activeTermTypeCount != activeTerms.size()) {
            throw new RestApiException(AuthErrorStatus.TERMS_CONFIGURATION_ERROR);
        }

        Map<Long, Term> activeTermsById = activeTerms.stream()
                .collect(Collectors.toMap(Term::getId, Function.identity()));
        Map<Long, Boolean> agreementByTermId = new HashMap<>();
        for (Consent consent : consents) {
            if (consent == null || consent.termId() == null
                    || agreementByTermId.putIfAbsent(consent.termId(), consent.agreed()) != null
                    || !activeTermsById.containsKey(consent.termId())) {
                throw new RestApiException(AuthErrorStatus.INVALID_TERM_AGREEMENT);
            }
        }

        boolean missingRequiredAgreement = requiredTerms.stream()
                .anyMatch(term -> !Boolean.TRUE.equals(agreementByTermId.get(term.getId())));
        if (missingRequiredAgreement) {
            throw new RestApiException(AuthErrorStatus.REQUIRED_TERMS_NOT_AGREED);
        }

        return activeTerms.stream()
                .filter(term -> Boolean.TRUE.equals(agreementByTermId.get(term.getId())))
                .toList();
    }

}
