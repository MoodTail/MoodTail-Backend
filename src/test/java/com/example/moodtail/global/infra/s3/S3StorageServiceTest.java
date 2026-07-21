package com.example.moodtail.global.infra.s3;

import com.example.moodtail.global.infra.s3.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    private static final String MANAGED_IMAGE_URL =
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png";

    @Mock
    private S3Client s3Client;

    private S3StorageService storageService;

    @BeforeEach
    void setUp() {
        S3Utilities utilities = S3Utilities.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
        lenient().when(s3Client.utilities()).thenReturn(utilities);
        storageService = new S3StorageService(
                s3Client,
                new S3Properties("moodtail", "ap-northeast-2", "", "")
        );
    }

    @Test
    void deletesAnImageGeneratedInTheConfiguredBucket() {
        storageService.deleteImage(MANAGED_IMAGE_URL);

        ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(request.capture());
        assertThat(request.getValue().bucket()).isEqualTo("moodtail");
        assertThat(request.getValue().key()).isEqualTo(
                "history/photos/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        );
    }

    @Test
    void rejectsAnImageFromAnotherBucket() {
        String imageUrl = MANAGED_IMAGE_URL.replace("moodtail.s3", "other-bucket.s3");

        assertThatThrownBy(() -> storageService.deleteImage(imageUrl))
                .isInstanceOf(S3StorageException.class);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png?versionId=malicious",
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png#malicious",
            "https://attacker@moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
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
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/photo.png",
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png",
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/../photos/"
                    + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png",
            "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/%2e%2e/photos/"
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
}
