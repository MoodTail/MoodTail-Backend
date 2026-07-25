package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareImageResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus.PAIR_SHARE_IMAGE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class PairRecommendationShareImageService {

    private static final String DIRECTORY = "recommendations/pair";

    private final PairRecommendationService pairRecommendationService;
    private final S3StorageService storageService;

    public PairRecommendationShareImageResponse uploadShareImage(
            Long userId,
            String partnerInviteCode,
            MultipartFile image
    ) {
        pairRecommendationService.validatePairRecommendationAvailable(userId, partnerInviteCode);

        try {
            String imageUrl = storageService.uploadImage(image, DIRECTORY);
            return new PairRecommendationShareImageResponse(imageUrl);
        } catch (S3StorageException exception) {
            log.error("Failed to upload pair recommendation share image", exception);
            throw new RestApiException(PAIR_SHARE_IMAGE_UNAVAILABLE);
        }
    }
}
