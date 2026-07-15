package com.example.moodtail.domain.history.storage;

import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import com.example.moodtail.global.infra.s3.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryPhotoStorageTest {

    @Mock
    private S3StorageService storageService;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Utilities s3Utilities;

    private HistoryPhotoStorage historyPhotoStorage;

    @BeforeEach
    void setUp() {
        s3Utilities = S3Utilities.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
        lenient().when(s3Client.utilities()).thenReturn(s3Utilities);
        historyPhotoStorage = new HistoryPhotoStorage(
                storageService,
                s3Client,
                new S3Properties("moodtail", "ap-northeast-2", "", "")
        );
    }

    @Test
    void delegatesUploadToTheSharedS3Service() {
        MultipartFile image = org.mockito.Mockito.mock(MultipartFile.class);
        when(storageService.uploadImage(image, "history/photos")).thenReturn("https://example.com/photo.png");

        assertThat(historyPhotoStorage.upload(image)).isEqualTo("https://example.com/photo.png");
    }

    @Test
    void deletesOnlyAnObjectOwnedByTheHistoryPhotoDirectory() {
        historyPhotoStorage.delete(
                "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
                        + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        );

        ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(request.capture());
        assertThat(request.getValue().bucket()).isEqualTo("moodtail");
        assertThat(request.getValue().key()).isEqualTo(
                "history/photos/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        );
    }

    @Test
    void rejectsAnObjectOutsideTheHistoryPhotoDirectory() {
        assertThatThrownBy(() -> historyPhotoStorage.delete(
                "https://moodtail.s3.ap-northeast-2.amazonaws.com/users/"
                        + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png"
        )).isInstanceOf(RestApiException.class);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
