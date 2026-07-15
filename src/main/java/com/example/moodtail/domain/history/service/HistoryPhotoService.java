package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.entity.HistoryPhoto;
import com.example.moodtail.domain.history.repository.HistoryImageWriter;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.history.storage.HistoryPhotoStorage;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.entity.ImageSourceType;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.INVALID_REQUEST;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.PHOTO_STORAGE_UNAVAILABLE;
import static com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.INVALID_IMAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryPhotoService {

    private final HistoryPhotoRepository historyPhotoRepository;
    private final HistoryImageWriter imageWriter;
    private final UserRepository userRepository;
    private final HistoryPhotoStorage photoStorage;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public HistoryPhotoResponse add(
            Long userId,
            String dateValue,
            MultipartFile image,
            String sourceTypeValue
    ) {
        LocalDate recordDate = HistoryDatePolicy.parse(dateValue);
        HistoryDatePolicy.validateRecordDate(recordDate, LocalDate.now(clock));
        ImageSourceType sourceType = parseSourceType(sourceTypeValue);

        String imageUrl;
        try {
            imageUrl = photoStorage.upload(image);
        } catch (S3StorageException exception) {
            log.error("Failed to upload history photo", exception);
            throw new RestApiException(PHOTO_STORAGE_UNAVAILABLE);
        }
        try {
            return transactionTemplate.execute(status -> persist(userId, recordDate, imageUrl, sourceType));
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
            String imageUrl,
            ImageSourceType sourceType
    ) {
        User user = userRepository.getReferenceById(userId);
        Image image = imageWriter.insert(imageUrl, sourceType);
        HistoryPhoto photo = historyPhotoRepository.save(HistoryPhoto.create(user, recordDate, image));
        return new HistoryPhotoResponse(photo.getId(), recordDate, sourceType, imageUrl);
    }

    private DeletedImage deleteFromDatabase(Long userId, LocalDate recordDate, Long photoId) {
        HistoryPhoto photo = historyPhotoRepository.findOwnedForUpdate(photoId, userId, recordDate)
                .orElseThrow(() -> new RestApiException(PHOTO_NOT_FOUND));
        Image image = photo.getImage();
        String imageUrl = image.getImageUrl();

        historyPhotoRepository.delete(photo);
        historyPhotoRepository.flush();
        boolean imageDeleted = imageWriter.deleteIfUnreferenced(image.getId());
        return new DeletedImage(imageUrl, imageDeleted);
    }

    private void deleteStoredImageSafely(String imageUrl) {
        try {
            photoStorage.delete(imageUrl);
        } catch (RuntimeException exception) {
            log.error("Failed to delete history photo from storage: imageUrl={}", imageUrl, exception);
        }
    }

    private ImageSourceType parseSourceType(String value) {
        if (value == null || value.isBlank()) {
            throw new RestApiException(INVALID_IMAGE);
        }
        try {
            ImageSourceType sourceType = ImageSourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
            if (sourceType == ImageSourceType.SYSTEM) {
                throw new RestApiException(INVALID_IMAGE);
            }
            return sourceType;
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(INVALID_IMAGE);
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
