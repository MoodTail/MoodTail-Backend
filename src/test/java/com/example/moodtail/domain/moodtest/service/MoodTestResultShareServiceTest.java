package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultShareCreateRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultShareCreateResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultSharePageResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.entity.SharedMoodTestResult;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.infra.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodTestResultShareServiceTest {

    @Mock UserRepository userRepository;
    @Mock SharedMoodTestResultRepository sharedMoodTestResultRepository;
    @Mock MoodTestResultService moodTestResultService;
    @Mock S3StorageService s3StorageService;

    private MoodTestResultShareService service;

    @BeforeEach
    void setUp() {
        service = new MoodTestResultShareService(
                userRepository,
                sharedMoodTestResultRepository,
                moodTestResultService,
                s3StorageService
        );
        ReflectionTestUtils.setField(service, "shareBaseUrl", "https://api.moodtail.com/");
        ReflectionTestUtils.setField(service, "shareFrontendBaseUrl", "https://moodtail.com/");
    }

    @Test
    void createsShareFromTasteProfileAndThumbnail() {
        User guest = User.createGuest(UUID.randomUUID().toString(), "게스트", LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(s3StorageService.uploadImage(any(), anyString()))
                .thenReturn("https://cdn.moodtail.com/share/result.png");
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "result.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        MoodTestResultShareCreateResponse response = service.createShare(1L, request(), thumbnail);

        assertThat(response.shareToken()).startsWith("r_");
        assertThat(response.shareUrl())
                .isEqualTo("https://api.moodtail.com/share/results/" + response.shareToken());
        ArgumentCaptor<SharedMoodTestResult> captor = ArgumentCaptor.forClass(SharedMoodTestResult.class);
        verify(sharedMoodTestResultRepository).saveAndFlush(captor.capture());
        SharedMoodTestResult savedResult = captor.getValue();
        assertThat(savedResult.getUser()).isSameAs(guest);
        assertThat(savedResult.getShareToken()).isEqualTo(response.shareToken());
        assertThat(savedResult.getThumbnailImageUrl())
                .isEqualTo("https://cdn.moodtail.com/share/result.png");
        assertThat(savedResult.toTasteProfile().sweetness()).isEqualByComparingTo("3.5");
    }

    @Test
    void returnsCalculatedResultForShareToken() {
        User guest = User.createGuest(UUID.randomUUID().toString(), "게스트", LocalDateTime.now());
        TasteProfile tasteProfile = TasteProfile.of(
                new BigDecimal("2.5"),
                new BigDecimal("3.5"),
                new BigDecimal("4.0"),
                new BigDecimal("4.5"),
                new BigDecimal("1.5")
        );
        SharedMoodTestResult sharedResult = SharedMoodTestResult.create(
                guest,
                "r_test",
                tasteProfile,
                "https://cdn.moodtail.com/share/result.png"
        );
        MoodTestResultResponse expected = MoodTestResultResponse.builder()
                .saved(false)
                .build();
        when(sharedMoodTestResultRepository.findByShareToken("r_test"))
                .thenReturn(Optional.of(sharedResult));
        when(moodTestResultService.calculateResult(tasteProfile)).thenReturn(expected);

        MoodTestResultResponse response = service.getSharedResult("r_test");

        assertThat(response).isSameAs(expected);
        verify(moodTestResultService).calculateResult(tasteProfile);
    }

    @Test
    void returnsSharePageMetadataForShareToken() {
        User guest = User.createGuest(UUID.randomUUID().toString(), "게스트", LocalDateTime.now());
        SharedMoodTestResult sharedResult = SharedMoodTestResult.create(
                guest,
                "r_test",
                requestTasteProfile(),
                "https://cdn.moodtail.com/share/result.png"
        );
        when(sharedMoodTestResultRepository.findByShareToken("r_test"))
                .thenReturn(Optional.of(sharedResult));

        MoodTestResultSharePageResponse response = service.getSharePage("r_test");

        assertThat(response.shareUrl())
                .isEqualTo("https://api.moodtail.com/share/results/r_test");
        assertThat(response.frontendUrl())
                .isEqualTo("https://moodtail.com/share/results/r_test");
        assertThat(response.thumbnailImageUrl())
                .isEqualTo("https://cdn.moodtail.com/share/result.png");
    }

    private MoodTestResultShareCreateRequest request() {
        return new MoodTestResultShareCreateRequest(
                new MoodTestResultShareCreateRequest.TasteProfileDto(
                        new BigDecimal("2.5"),
                        new BigDecimal("3.5"),
                        new BigDecimal("4.0"),
                        new BigDecimal("4.5"),
                        new BigDecimal("1.5")
                )
        );
    }

    private TasteProfile requestTasteProfile() {
        return TasteProfile.of(
                new BigDecimal("2.5"),
                new BigDecimal("3.5"),
                new BigDecimal("4.0"),
                new BigDecimal("4.5"),
                new BigDecimal("1.5")
        );
    }
}
