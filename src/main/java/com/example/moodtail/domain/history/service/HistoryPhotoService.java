package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.entity.HistoryPhoto;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.entity.ImageSourceType;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.INVALID_REQUEST;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_LIMIT_EXCEEDED;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_STORAGE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryPhotoService {

    private static final String PHOTO_DIRECTORY = "public/history/photos";
    private static final int MAX_PHOTOS_PER_DATE = 5;
    // The shared images table still requires one of its legacy non-null source values.
    private static final ImageSourceType HISTORY_PHOTO_SOURCE_TYPE = ImageSourceType.GALLERY;

    private final HistoryPhotoRepository historyPhotoRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final S3StorageService storageService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public HistoryPhotoResponse add(
            Long userId,
            String dateValue,
            MultipartFile image
    ) {
        LocalDate recordDate = HistoryDatePolicy.parse(dateValue);
        HistoryDatePolicy.validateRecordDate(recordDate, LocalDate.now(clock));
        validatePhotoLimit(userId, recordDate);

        String imageUrl;
        try {
            imageUrl = storageService.uploadImage(image, PHOTO_DIRECTORY);
        } catch (S3StorageException exception) {
            log.error("Failed to upload history photo", exception);
            throw new RestApiException(PHOTO_STORAGE_UNAVAILABLE);
        }
        try {
            return transactionTemplate.execute(status -> persist(userId, recordDate, imageUrl));
        } catch (RuntimeException exception) {
            deleteStoredImageSafely(imageUrl);
            throw exception;
        }
    }

    public void delete(Long userId, String dateValue, Long photoId) {
        LocalDate recordDate = HistoryDatePolicy.parse(dateValue);
        HistoryDatePolicy.validateRecordDate(recordDate, LocalDate.now(clock));
        validateId(photoId);

        DeletedImage deletedImage = transactionTemplate.execute(status -> deleteFromDatabase(
                userId,
                recordDate,
                photoId
        ));
        if (deletedImage == null) {
            throw new IllegalStateException("Photo deletion transaction returned no result");
        }
        if (deletedImage.imageDeleted()) {
            deleteStoredImageSafely(deletedImage.imageUrl());
        }
    }

    private HistoryPhotoResponse persist(
            Long userId,
            LocalDate recordDate,
            String imageUrl
    ) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        validatePhotoLimit(userId, recordDate);
        Image image = imageRepository.save(Image.create(imageUrl, HISTORY_PHOTO_SOURCE_TYPE));
        HistoryPhoto photo = historyPhotoRepository.save(HistoryPhoto.create(user, recordDate, image));
        return new HistoryPhotoResponse(
                photo.getId(),
                recordDate,
                imageUrl
        );
    }

    private void validatePhotoLimit(Long userId, LocalDate recordDate) {
        if (historyPhotoRepository.countByUserIdAndRecordDate(userId, recordDate)
                >= MAX_PHOTOS_PER_DATE) {
            throw new RestApiException(PHOTO_LIMIT_EXCEEDED);
        }
    }

    private DeletedImage deleteFromDatabase(Long userId, LocalDate recordDate, Long photoId) {
        HistoryPhoto photo = historyPhotoRepository.findOwnedForUpdate(photoId, userId, recordDate)
                .orElseThrow(() -> new RestApiException(PHOTO_NOT_FOUND));
        Image image = photo.getImage();
        String imageUrl = image.getImageUrl();

        historyPhotoRepository.delete(photo);
        historyPhotoRepository.flush();
        if (historyPhotoRepository.existsByImageId(image.getId())) {
            return new DeletedImage(imageUrl, false);
        }

        imageRepository.delete(image);
        imageRepository.flush();
        return new DeletedImage(imageUrl, true);
    }

    private void deleteStoredImageSafely(String imageUrl) {
        try {
            storageService.deleteImage(imageUrl);
        } catch (RuntimeException exception) {
            log.error("Failed to delete history photo from storage: imageUrl={}", imageUrl, exception);
        }
    }

    private void validateId(Long id) {
        if (id == null || id < 1) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    private record DeletedImage(String imageUrl, boolean imageDeleted) {
    }
}
