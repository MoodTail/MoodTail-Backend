package com.example.moodtail.domain.image.service;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.ImageErrorStatus;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3StorageService storageService;

    @Transactional(readOnly = true)
    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RestApiException(ImageErrorStatus.IMAGE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String getImageUrl(Long imageId) {
        if (imageId == null) {
            return null;
        }
        return getImage(imageId).getImageUrl();
    }

    public StorageCleanupResult deleteImagesFromStorage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return new StorageCleanupResult(0, 0);
        }

        int deleted = 0;
        int failed = 0;
        for (String imageUrl : imageUrls.stream().distinct().toList()) {
            try {
                storageService.deleteImage(imageUrl);
                deleted++;
            } catch (S3StorageException exception) {
                failed++;
                log.error(
                        "Manual image cleanup required: imageUrl={}",
                        imageUrl,
                        exception
                );
            }
        }
        return new StorageCleanupResult(deleted, failed);
    }

    public record StorageCleanupResult(int deleted, int failed) {
    }
}
