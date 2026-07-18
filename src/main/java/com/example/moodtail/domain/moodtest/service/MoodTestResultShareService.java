package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultShareCreateRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultShareCreateResponse;
import com.example.moodtail.domain.moodtest.entity.SharedMoodTestResult;
import com.example.moodtail.domain.moodtest.repository.SharedMoodTestResultRepository;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.Base64;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MoodTestResultShareService {

    private static final String SHARE_IMAGE_DIRECTORY = "public/share/test-results";
    private static final String SHARE_PATH = "/share/results/";
    private static final int TOKEN_BYTE_LENGTH = 18;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final SharedMoodTestResultRepository sharedMoodTestResultRepository;
    private final S3StorageService s3StorageService;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    @Transactional
    public MoodTestResultShareCreateResponse createShare(
            Long userId,
            MoodTestResultShareCreateRequest request,
            MultipartFile thumbnail
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        TasteProfile tasteProfile = toTasteProfile(request.tasteProfile());
        String shareToken = generateShareToken();
        String thumbnailImageUrl = s3StorageService.uploadImage(thumbnail, SHARE_IMAGE_DIRECTORY);

        SharedMoodTestResult sharedResult = SharedMoodTestResult.create(
                user,
                shareToken,
                tasteProfile,
                thumbnailImageUrl
        );
        sharedMoodTestResultRepository.saveAndFlush(sharedResult);

        return new MoodTestResultShareCreateResponse(
                shareToken,
                normalizeBaseUrl(shareBaseUrl) + SHARE_PATH + shareToken
        );
    }

    private TasteProfile toTasteProfile(MoodTestResultShareCreateRequest.TasteProfileDto tasteProfile) {
        return TasteProfile.of(
                tasteProfile.alcoholIntensity(),
                tasteProfile.sweetness(),
                tasteProfile.sourness(),
                tasteProfile.refreshing(),
                tasteProfile.bitterness()
        );
    }

    private String generateShareToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return "r_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
