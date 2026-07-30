package com.example.moodtail.domain.term.service;

import com.example.moodtail.domain.term.dto.response.TermsResponse;
import com.example.moodtail.domain.term.entity.Term;
import com.example.moodtail.domain.term.entity.TermType;
import com.example.moodtail.domain.term.repository.TermRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private TermService termService;

    @Test
    void getAllActiveTerms() {
        given(termRepository.findByActiveTrueOrderByIdAsc()).willReturn(List.of(
                term(1L, TermType.SERVICE, true),
                term(2L, TermType.PRIVACY, true)
        ));

        TermsResponse response = termService.getTerms(null);

        assertThat(response.terms()).hasSize(2);
        assertThat(response.terms().get(0).termType()).isEqualTo(TermType.SERVICE);
        verify(termRepository).findByActiveTrueOrderByIdAsc();
    }

    @Test
    void getActiveTermsByType() {
        given(termRepository.findByTermTypeAndActiveTrueOrderByIdAsc(TermType.PRIVACY))
                .willReturn(List.of(term(2L, TermType.PRIVACY, true)));

        TermsResponse response = termService.getTerms("PRIVACY");

        assertThat(response.terms()).singleElement()
                .extracting(TermsResponse.TermResponse::termType)
                .isEqualTo(TermType.PRIVACY);
        verify(termRepository).findByTermTypeAndActiveTrueOrderByIdAsc(TermType.PRIVACY);
    }

    @Test
    void rejectInvalidTermType() {
        assertThatThrownBy(() -> termService.getTerms("UNKNOWN"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("TERM400")
                );

        verifyNoInteractions(termRepository);
    }

    @Test
    void throwWhenActiveTermsDoNotExist() {
        given(termRepository.findByActiveTrueOrderByIdAsc()).willReturn(List.of());

        assertThatThrownBy(() -> termService.getTerms(null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("TERM404")
                );
    }

    private Term term(Long id, TermType termType, boolean active) {
        return Term.builder()
                .id(id)
                .termType(termType)
                .title("title")
                .content("content")
                .required(true)
                .version("1.0.0")
                .active(active)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
