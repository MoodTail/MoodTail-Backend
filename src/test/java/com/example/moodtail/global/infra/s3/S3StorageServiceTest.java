package com.example.moodtail.global.infra.s3;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    private static final String MANAGED_IMAGE_URL =
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png";

    @Mock
    private S3Client s3Client;

    private S3StorageService storageService;

    @BeforeEach
    void setUp() {
        S3Utilities utilities = S3Utilities.builder()
                .region(Region.AP_SOUTHEAST_2)
                .build();
        lenient().when(s3Client.utilities()).thenReturn(utilities);
        storageService = new S3StorageService(
                s3Client,
                new S3Properties("moodtail-bucket", "ap-southeast-2", "", "")
        );
    }

    @ParameterizedTest
    @MethodSource("supportedImageFiles")
    void uploadsAnImageWhenMagicBytesMatchTheDeclaredType(
            String filename,
            String contentType,
            byte[] content
    ) {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                filename,
                contentType,
                content
        );

        storageService.uploadImage(image, "public/test-images");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().contentType()).isEqualTo(contentType);
        assertThat(request.getValue().key()).endsWith("." + filename.substring(filename.lastIndexOf('.') + 1));
    }

    @ParameterizedTest
    @MethodSource("invalidImageFiles")
    void rejectsAnImageWhenMagicBytesDoNotMatchTheDeclaredType(
            String filename,
            String contentType,
            byte[] content
    ) {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                filename,
                contentType,
                content
        );

        assertThatThrownBy(() -> storageService.uploadImage(image, "public/test-images"))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode()).isEqualTo("IMAGE400")
                );

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deletesAnImageGeneratedInTheConfiguredBucket() {
        storageService.deleteImage(MANAGED_IMAGE_URL);

        ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(request.capture());
        assertThat(request.getValue().bucket()).isEqualTo("moodtail-bucket");
        assertThat(request.getValue().key()).isEqualTo(
                "public/history/photos/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        );
    }

    @Test
    void rejectsAnImageFromAnotherBucket() {
        String imageUrl = MANAGED_IMAGE_URL.replace("moodtail-bucket.s3", "other-bucket.s3");

        assertThatThrownBy(() -> storageService.deleteImage(imageUrl))
                .isInstanceOf(S3StorageException.class);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png?versionId=malicious",
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png#malicious",
            "https://attacker@moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
    })
    void rejectsAnImageUrlContainingQueryFragmentOrUserInfo(String imageUrl) {
        assertThatThrownBy(() -> storageService.deleteImage(imageUrl))
                .isInstanceOf(S3StorageException.class);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/history/photos/photo.png",
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png",
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/../photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png",
            "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/%2e%2e/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
    })
    void rejectsAnObjectNotGeneratedByTheImageUploadService(String imageUrl) {
        assertThatThrownBy(() -> storageService.deleteImage(imageUrl))
                .isInstanceOf(S3StorageException.class);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void wrapsS3DeletionFailure() {
        doThrow(S3Exception.builder().message("storage unavailable").build())
                .when(s3Client)
                .deleteObject(any(DeleteObjectRequest.class));

        assertThatThrownBy(() -> storageService.deleteImage(MANAGED_IMAGE_URL))
                .isInstanceOf(S3StorageException.class)
                .hasMessage("Failed to delete image from S3");
    }

    @Test
    void loadsAnImageGeneratedInTheConfiguredBucket() {
        byte[] content = {1, 2, 3};
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().contentType("image/png").build(),
                        content
                ));

        S3StorageService.StoredImage image = storageService.getImage(MANAGED_IMAGE_URL);

        ArgumentCaptor<GetObjectRequest> request = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(request.capture());
        assertThat(request.getValue().bucket()).isEqualTo("moodtail-bucket");
        assertThat(request.getValue().key()).isEqualTo(
                "public/history/photos/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        );
        assertThat(image.content()).containsExactly(content);
        assertThat(image.contentType()).isEqualTo("image/png");
    }

    @Test
    void rejectsStoredObjectWithUnsupportedContentType() {
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().contentType("text/html").build(),
                        new byte[]{1, 2, 3}
                ));

        assertThatThrownBy(() -> storageService.getImage(MANAGED_IMAGE_URL))
                .isInstanceOf(S3StorageException.class)
                .hasMessage("Stored S3 object is not a supported image");
    }

    @Test
    void wrapsS3ImageLoadingFailure() {
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("storage unavailable").build());

        assertThatThrownBy(() -> storageService.getImage(MANAGED_IMAGE_URL))
                .isInstanceOf(S3StorageException.class)
                .hasMessage("Failed to load image from S3");
    }

    private static Stream<Arguments> supportedImageFiles() {
        return Stream.of(
                Arguments.of("image.png", "image/png", new byte[]{
                        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
                }),
                Arguments.of("image.jpg", "image/jpeg", new byte[]{
                        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1
                }),
                Arguments.of("image.webp", "image/webp", new byte[]{
                        0x52, 0x49, 0x46, 0x46, 0x04, 0x00, 0x00, 0x00,
                        0x57, 0x45, 0x42, 0x50
                })
        );
    }

    private static Stream<Arguments> invalidImageFiles() {
        return Stream.of(
                Arguments.of(
                        "disguised.png",
                        "image/png",
                        "not-an-image".getBytes(StandardCharsets.UTF_8)
                ),
                Arguments.of("mismatched.jpg", "image/jpeg", new byte[]{
                        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
                }),
                Arguments.of("truncated.webp", "image/webp", new byte[]{
                        0x52, 0x49, 0x46, 0x46, 0x04, 0x00, 0x00, 0x00, 0x57, 0x45
                })
        );
    }
}
