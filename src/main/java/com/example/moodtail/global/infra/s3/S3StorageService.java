package com.example.moodtail.global.infra.s3;

import com.example.moodtail.global.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class S3StorageService {

    private static final Pattern DIRECTORY_PATTERN = Pattern.compile("[A-Za-z0-9/_-]+");
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("[A-Za-z0-9]+");

    private final S3Client s3Client;
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

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new S3StorageException("Image must not be empty");
        }
        if (!StringUtils.hasText(properties.bucket())) {
            throw new S3StorageException("S3 bucket is not configured");
        }
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
        if (!StringUtils.hasText(extension) || !EXTENSION_PATTERN.matcher(extension).matches()) {
            return "";
        }
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
}
