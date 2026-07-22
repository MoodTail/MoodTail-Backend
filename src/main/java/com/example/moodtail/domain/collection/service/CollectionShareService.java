package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import
        com.example.moodtail.domain.collection.dto.response.CollectionShareCreateResponse;
import
        com.example.moodtail.domain.collection.dto.response.CollectionSharePageResponse;
import com.example.moodtail.domain.collection.entity.CollectionShare;
import com.example.moodtail.domain.collection.repository.CollectionShareRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.Base64;

import static
        com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static
        com.example.moodtail.global.common.exception.code.status.CollectionErrorStatus.COLLECTION_SHARE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionShareService {
    private static final String SHARE_IMAGE_DIRECTORY =
            "public/share/collections";

    private static final String SHARE_PATH =
            "/share/collections/";

    private static final int TOKEN_BYTE_LENGTH = 18;

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserRepository userRepository;
    private final CollectionShareRepository collectionShareRepository;
    private final CollectionService collectionService;
    private final S3StorageService s3StorageService;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    @Value("${app.share.frontend-base-url}")
    private String shareFrontendBaseUrl;

    /**
     * 프론트에서 만든 도감 공유 이미지를 저장한다.
     *
     * 최초 공유:
     * - 새로운 공유 토큰 생성
     * - CollectionShare 생성
     *
     * 재공유:
     * - 기존 공유 토큰 유지
     * - 이미지 URL 교체
     * - 버전 증가
     */
    @Transactional
    public CollectionShareCreateResponse createOrUpdateShare(
            Long userId,
            MultipartFile thumbnail
    ) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(
                        () -> new RestApiException(USER_NOT_FOUND)
                );

        collectionService.getCollection(userId);

        // 기존 공유 데이터가 있는지 먼저 확인한다.
        CollectionShare collectionShare =
                collectionShareRepository.findByUserId(userId)
                        .orElse(null);

        String oldThumbnailImageUrl =
                collectionShare == null
                        ? null
                        : collectionShare.getThumbnailImageUrl();

        /*
         * 새 이미지를 S3에 먼저 업로드한다.
         *
         * 업로드에 실패하면 DB와 기존 이미지에는 변화가 없다.
         */
        String newThumbnailImageUrl =
                s3StorageService.uploadImage(
                        thumbnail,
                        SHARE_IMAGE_DIRECTORY
                );

        /*
         * DB 트랜잭션 결과에 따라 S3 이미지를 정리하도록 등록한다.
         *
         * 커밋 성공:
         * - 기존 이미지 삭제
         *
         * 롤백:
         * - 새로 업로드한 이미지 삭제
         */
        registerStorageCleanup(
                oldThumbnailImageUrl,
                newThumbnailImageUrl
        );

        if (collectionShare == null) {
            //최초 공유이므로 토큰과 공유 데이터를 생성한다.
            collectionShare = CollectionShare.create(
                    user,
                    generateShareToken(),
                    newThumbnailImageUrl
            );
        } else {
            //기존 공유 토큰은 유지한다. 이미지 URL과 버전만 변경한다.
            collectionShare.updateThumbnailImageUrl(
                    newThumbnailImageUrl
            );
        }

        collectionShareRepository.saveAndFlush(
                collectionShare
        );

        return new CollectionShareCreateResponse(
                collectionShare.getShareToken(),
                createShareUrl(collectionShare)
        );
    }

    //공개 공유 토큰으로 사용자의 최신 도감 상태를 조회한다.
    @Transactional(readOnly = true)
    public CollectionResponse getSharedCollection(
            String shareToken
    ) {
        CollectionShare collectionShare =
                findCollectionShare(shareToken);

        return collectionService.getCollection(
                collectionShare.getUser().getId()
        );
    }

    // OG 공유 페이지에 필요한 정보를 반환한다.
    @Transactional(readOnly = true)
    public CollectionSharePageResponse getSharePage(
            String shareToken
    ) {
        CollectionShare collectionShare =
                findCollectionShare(shareToken);

        String versionQuery =
                "?v=" + collectionShare.getVersion();

        String sharePath =
                SHARE_PATH
                        + collectionShare.getShareToken()
                        + versionQuery;

        return new CollectionSharePageResponse(
                normalizeBaseUrl(shareBaseUrl)
                        + sharePath,

                normalizeBaseUrl(shareFrontendBaseUrl)
                        + sharePath,

                collectionShare.getThumbnailImageUrl()
        );
    }

    // 공유 토큰으로 CollectionShare를 조회한다.
    private CollectionShare findCollectionShare(
            String shareToken
    ) {
        return collectionShareRepository
                .findByShareToken(shareToken)
                .orElseThrow(
                        () -> new RestApiException(
                                COLLECTION_SHARE_NOT_FOUND
                        )
                );
    }

    /**
     * 클라이언트에 반환할 백엔드 공유 URL을 생성한다.
     *
     * version을 쿼리 파라미터로 붙여
     * SNS 링크 미리보기 캐시 갱신을 유도한다.
     */
    private String createShareUrl(
            CollectionShare collectionShare
    ) {
        return normalizeBaseUrl(shareBaseUrl)
                + SHARE_PATH
                + collectionShare.getShareToken()
                + "?v="
                + collectionShare.getVersion();
    }

    // 사용자 전용 공유 토큰을 생성한다.
    private String generateShareToken() {
        byte[] randomBytes =
                new byte[TOKEN_BYTE_LENGTH];

        SECURE_RANDOM.nextBytes(randomBytes);

        return "c_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    // DB 트랜잭션 결과에 맞춰 S3 이미지를 정리한다.
    private void registerStorageCleanup(
            String oldThumbnailImageUrl,
            String newThumbnailImageUrl
    ) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    // DB 커밋 성공 후 기존 이미지를 삭제한다.
                    @Override
                    public void afterCommit() {
                        deleteThumbnailBestEffort(
                                oldThumbnailImageUrl,
                                "previous"
                        );
                    }

                    // DB 롤백 시 새로 업로드한 이미지를 삭제한다.
                    @Override
                    public void afterCompletion(int status) {
                        if (status
                                == TransactionSynchronization.STATUS_ROLLED_BACK) {
                            deleteThumbnailBestEffort(
                                    newThumbnailImageUrl,
                                    "new"
                            );
                        }
                    }
                }
        );
    }

    // S3 이미지 삭제 실패가 공유 요청 전체를 실패시키지 않도록 한다.
    private void deleteThumbnailBestEffort(
            String thumbnailImageUrl,
            String imageType
    ) {
        if (thumbnailImageUrl == null
                || thumbnailImageUrl.isBlank()) {
            return;
        }

        try {
            s3StorageService.deleteImage(
                    thumbnailImageUrl
            );
        } catch (S3StorageException exception) {
            log.warn(
                    "Failed to delete collection share image: "
                            + "imageType={}, imageUrl={}",
                    imageType,
                    thumbnailImageUrl,
                    exception
            );
        }
    }

    // baseUrl 마지막의 슬래시를 제거한다.
    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(
                0,
                baseUrl.length() - 1
        )
                : baseUrl;
    }
}
