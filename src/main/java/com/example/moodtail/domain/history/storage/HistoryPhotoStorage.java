package com.example.moodtail.domain.history.storage;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.net.URI;

import static com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.INVALID_IMAGE;

@Component
@RequiredArgsConstructor
public class HistoryPhotoStorage {

    private static final String DIRECTORY = "history/photos";
    private static final String DIRECTORY_PREFIX = DIRECTORY + "/";
    private static final String IMAGE_FILE_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                    + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(png|jpg|jpeg|webp)";

    private final S3StorageService storageService;
    private final S3Client s3Client;
    private final S3Properties properties;

    public String upload(MultipartFile image) {
        return storageService.uploadImage(image, DIRECTORY);
    }

    public void delete(String imageUrl) {
        String objectKey = resolveOwnedObjectKey(imageUrl);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException exception) {
            throw new S3StorageException("Failed to delete history photo from S3", exception);
        }
    }

    private String resolveOwnedObjectKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || properties.bucket() == null || properties.bucket().isBlank()) {
            throw new RestApiException(INVALID_IMAGE);
        }

        try {
            URI imageUri = URI.create(imageUrl);
            S3Uri s3Uri = s3Client.utilities().parseUri(imageUri);
            String objectKey = s3Uri.key().orElse("");
            if (!properties.bucket().equals(s3Uri.bucket().orElse(null))
                    || imageUri.getQuery() != null
                    || imageUri.getFragment() != null
                    || imageUri.getUserInfo() != null
                    || !objectKey.startsWith(DIRECTORY_PREFIX)) {
                throw new RestApiException(INVALID_IMAGE);
            }

            String filename = objectKey.substring(DIRECTORY_PREFIX.length());
            if (!filename.matches(IMAGE_FILE_PATTERN)) {
                throw new RestApiException(INVALID_IMAGE);
            }
            return objectKey;
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(INVALID_IMAGE);
        }
    }
}
