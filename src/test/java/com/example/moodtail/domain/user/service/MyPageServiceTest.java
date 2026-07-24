package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.MyPageProjection;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyPageProjection projection;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    @DisplayName("게스트 사용자는 마이페이지를 조회할 수 없다")
    void guestCannotGetMyPage() {
        assertThatThrownBy(() -> myPageService.getMyPage(1L, UserRole.GUEST.name()))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH027")
                );

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("로그인 사용자의 대표 타입과 활동 통계를 조회한다")
    void getMyPage() {
        given(projection.getUserId()).willReturn(1L);
        given(projection.getNickname()).willReturn("username");
        given(projection.getMoodTypeId()).willReturn(3L);
        given(projection.getMoodTypeCode()).willReturn("FRESH_SPARK");
        given(projection.getMoodTypeName()).willReturn("상큼주의자");
        given(projection.getCharacterImageUrl()).willReturn("https://cdn.moodtail.com/types/fresh-spark.png");
        given(projection.getTotalTestCount()).willReturn(8L);
        given(projection.getMonthlyRecordCount()).willReturn(3L);
        given(projection.getUnlockedMoodTypeCount()).willReturn(4L);
        given(userRepository.findMyPageByUserId(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        ))
                .willReturn(Optional.of(projection));

        MyPageResponse response = myPageService.getMyPage(1L, UserRole.USER.name());

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("username");
        assertThat(response.representativeMoodType()).isNotNull();
        assertThat(response.representativeMoodType().moodTypeId()).isEqualTo(3L);
        assertThat(response.representativeMoodType().characterImageUrl())
                .isEqualTo("https://cdn.moodtail.com/types/fresh-spark.png");
        assertThat(response.totalTestCount()).isEqualTo(8L);
        assertThat(response.monthlyRecordCount()).isEqualTo(3L);
        assertThat(response.unlockedMoodTypeCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("대표 타입이 없는 사용자는 대표 타입을 null로 반환한다")
    void getMyPageWithoutRepresentativeMoodType() {
        given(projection.getUserId()).willReturn(2L);
        given(projection.getNickname()).willReturn("new-user");
        given(projection.getMoodTypeId()).willReturn(null);
        given(projection.getTotalTestCount()).willReturn(0L);
        given(projection.getMonthlyRecordCount()).willReturn(0L);
        given(projection.getUnlockedMoodTypeCount()).willReturn(0L);
        given(userRepository.findMyPageByUserId(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        ))
                .willReturn(Optional.of(projection));

        MyPageResponse response = myPageService.getMyPage(2L, UserRole.USER.name());

        assertThat(response.representativeMoodType()).isNull();
        assertThat(response.totalTestCount()).isZero();
        assertThat(response.monthlyRecordCount()).isZero();
        assertThat(response.unlockedMoodTypeCount()).isZero();
    }

    @Test
    @DisplayName("이번 달 기록은 월초 이상 다음 달 월초 미만 범위로 조회한다")
    void useMonthlyDateRange() {
        given(projection.getUserId()).willReturn(1L);
        given(projection.getTotalTestCount()).willReturn(0L);
        given(projection.getMonthlyRecordCount()).willReturn(0L);
        given(projection.getUnlockedMoodTypeCount()).willReturn(0L);
        given(userRepository.findMyPageByUserId(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class)
        )).willReturn(Optional.of(projection));

        myPageService.getMyPage(1L, UserRole.USER.name());

        ArgumentCaptor<LocalDate> monthStartCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> nextMonthStartCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(userRepository).findMyPageByUserId(
                org.mockito.ArgumentMatchers.eq(1L),
                monthStartCaptor.capture(),
                nextMonthStartCaptor.capture()
        );
        assertThat(monthStartCaptor.getValue().getDayOfMonth()).isEqualTo(1);
        assertThat(nextMonthStartCaptor.getValue()).isEqualTo(monthStartCaptor.getValue().plusMonths(1));
    }

}
