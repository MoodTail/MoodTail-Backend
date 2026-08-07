package com.example.moodtail.global.infra.s3;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Uri;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.IMAGE_TOO_LARGE;
import static com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.INVALID_IMAGE;

@Component
@RequiredArgsConstructor
public class S3StorageService {

    private static final Pattern DIRECTORY_PATTERN = Pattern.compile("[A-Za-z0-9/_-]+");
    private static final Pattern IMAGE_FILE_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                    + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(png|jpg|jpeg|webp)"
    );
    private static final Duration PRESIGNED_GET_URL_DURATION = Duration.ofHours(1);
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Map<String, Set<String>> ALLOWED_IMAGE_EXTENSIONS = Map.of(
            "image/png", Set.of("png"),
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/webp", Set.of("webp")
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public String uploadImage(MultipartFile image, String directory) {
        validateImage(image);
        String objectKey = createObjectKey(directory, image.getOriginalFilename());
        PutObjectRequest request = createPutObjectRequest(objectKey, image.getContentType());

        try (InputStream inputStream = image.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, image.getSize()));
            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(properties.bucket())
                            .key(objectKey)
                            .build())
                    .toExternalForm();
        } catch (IOException | SdkException exception) {
            throw new S3StorageException("Failed to upload image to S3", exception);
        }
    }

    public void deleteImage(String imageUrl) {
        String objectKey = resolveObjectKey(imageUrl);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
        } catch (SdkException exception) {
            throw new S3StorageException("Failed to delete image from S3", exception);
        }
    }

    public String createPresignedGetUrl(String imageUrl) {
        String objectKey = resolveObjectKey(imageUrl);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_GET_URL_DURATION)
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            return s3Presigner.presignGetObject(presignRequest)
                    .url()
                    .toExternalForm();
        } catch (SdkException exception) {
            throw new S3StorageException("Failed to create presigned image URL", exception);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RestApiException(INVALID_IMAGE);
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new RestApiException(IMAGE_TOO_LARGE);
        }

        String contentType = image.getContentType();
        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        if (!StringUtils.hasText(contentType)
                || !StringUtils.hasText(extension)
                || !isAllowedImageExtension(contentType, extension)) {
            throw new RestApiException(INVALID_IMAGE);
        }
        if (!StringUtils.hasText(properties.bucket())) {
            throw new S3StorageException("S3 bucket is not configured");
        }
    }

    private boolean isAllowedImageExtension(String contentType, String extension) {
        Set<String> allowedExtensions = ALLOWED_IMAGE_EXTENSIONS.get(contentType.toLowerCase(Locale.ROOT));
        return allowedExtensions != null
                && allowedExtensions.contains(extension.toLowerCase(Locale.ROOT));
    }

    private String createObjectKey(String directory, String originalFilename) {
        String normalizedDirectory = normalizeDirectory(directory);
        return normalizedDirectory + "/" + UUID.randomUUID() + resolveExtension(originalFilename);
    }

    private String normalizeDirectory(String directory) {
        if (!StringUtils.hasText(directory)) {
            throw new S3StorageException("S3 directory must not be blank");
        }

        String normalized = directory.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (!StringUtils.hasText(normalized)
                || normalized.contains("..")
                || !DIRECTORY_PATTERN.matcher(normalized).matches()) {
            throw new S3StorageException("Invalid S3 directory");
        }
        return normalized;
    }

    private String resolveExtension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        return "." + extension.toLowerCase(Locale.ROOT);
    }

    private PutObjectRequest createPutObjectRequest(String objectKey, String contentType) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey);
        if (StringUtils.hasText(contentType)) {
            builder.contentType(contentType);
        }
        return builder.build();
    }

    private String resolveObjectKey(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !StringUtils.hasText(properties.bucket())) {
            throw new S3StorageException("Invalid S3 image URL");
        }

        try {
            URI imageUri = URI.create(imageUrl);
            if (imageUri.getUserInfo() != null
                    || imageUri.getRawQuery() != null
                    || imageUri.getRawFragment() != null) {
                throw new S3StorageException("Invalid S3 image URL");
            }

            S3Uri s3Uri = s3Client.utilities().parseUri(imageUri);
            String bucket = s3Uri.bucket().orElse("");
            String objectKey = s3Uri.key().orElse("");
            if (!properties.bucket().equals(bucket) || !isGeneratedImageObjectKey(objectKey)) {
                throw new S3StorageException("Image URL does not belong to a managed S3 image");
            }
            return objectKey;
        } catch (IllegalArgumentException exception) {
            throw new S3StorageException("Invalid S3 image URL", exception);
        }
    }

    private boolean isGeneratedImageObjectKey(String objectKey) {
        int filenameSeparator = objectKey.lastIndexOf('/');
        if (filenameSeparator < 1 || filenameSeparator == objectKey.length() - 1) {
            return false;
        }

        String directory = objectKey.substring(0, filenameSeparator);
        String filename = objectKey.substring(filenameSeparator + 1);
        try {
            return directory.equals(normalizeDirectory(directory))
                    && IMAGE_FILE_PATTERN.matcher(filename).matches();
        } catch (S3StorageException exception) {
            return false;
        }
    }
}
