package com.example.moodtail.domain.term.dto.response;

import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;

import java.util.List;

public record TermsResponse(
        List<TermResponse> terms
) {

    public static TermsResponse from(List<Term> terms) {
        return new TermsResponse(terms.stream()
                .map(TermResponse::from)
                .toList());
    }

    public record TermResponse(
            Long termId,
            TermType termType,
            String title,
            String version,
            boolean required,
            String content
    ) {

        private static TermResponse from(Term term) {
            return new TermResponse(
                    term.getId(),
                    term.getTermType(),
                    term.getTitle(),
                    term.getVersion(),
                    term.isRequired(),
                    term.getContent()
            );
        }
    }
}
