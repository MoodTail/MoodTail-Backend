package com.example.moodtail.domain.inquiry.service;

import com.example.moodtail.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.moodtail.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.moodtail.domain.inquiry.entity.Inquiry;
import com.example.moodtail.domain.inquiry.entity.InquiryStatus;
import com.example.moodtail.domain.inquiry.repository.InquiryRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Inquiry savedInquiry;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("비회원 문의는 연락처 이메일과 함께 저장한다")
    void createGuestInquiry() {
        givenSavedInquiry();

        InquiryCreateResponse response = inquiryService.createInquiry(
                new InquiryCreateRequest("BUG", "결과 페이지 이미지가 보이지 않습니다.", "guest@example.com"),
                null
        );

        ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(inquiryCaptor.capture());
        assertThat(inquiryCaptor.getValue().getUser()).isNull();
        assertThat(inquiryCaptor.getValue().getContactEmail()).isEqualTo("guest@example.com");
        assertThat(response.inquiryId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(InquiryStatus.PENDING);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("로그인 사용자는 연락처 이메일 없이 문의를 저장할 수 있다")
    void createUserInquiryWithoutContactEmail() {
        User user = org.mockito.Mockito.mock(User.class);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        givenSavedInquiry();

        inquiryService.createInquiry(
                new InquiryCreateRequest("FEEDBACK", "추천 결과가 기대와 조금 달라요.", null),
                new PrincipalDetails(1L, UserRole.USER)
        );

        ArgumentCaptor<Inquiry> inquiryCaptor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(inquiryCaptor.capture());
        assertThat(inquiryCaptor.getValue().getUser()).isSameAs(user);
        assertThat(inquiryCaptor.getValue().getContactEmail()).isNull();
    }

    @Test
    @DisplayName("게스트 토큰 사용자는 비회원과 같이 연락처 이메일이 필요하다")
    void guestTokenRequiresContactEmail() {
        assertThatThrownBy(() -> inquiryService.createInquiry(
                new InquiryCreateRequest("ACCOUNT", "게스트 계정 관련 문의입니다.", null),
                new PrincipalDetails(1L, UserRole.GUEST)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("INQUIRY400")
        );

        verifyNoInteractions(inquiryRepository, userRepository);
    }

    @Test
    @DisplayName("본문이 10자 미만이면 문의를 저장하지 않는다")
    void rejectShortContent() {
        assertThatThrownBy(() -> inquiryService.createInquiry(
                new InquiryCreateRequest("ETC", "짧은 문의", "guest@example.com"),
                null
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("INQUIRY400")
        );

        verifyNoInteractions(inquiryRepository, userRepository);
    }

    private void givenSavedInquiry() {
        given(savedInquiry.getId()).willReturn(1L);
        given(savedInquiry.getStatus()).willReturn(InquiryStatus.PENDING);
        given(savedInquiry.getCreatedAt()).willReturn(LocalDateTime.of(2026, 7, 10, 18, 0));
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(savedInquiry);
    }
}
