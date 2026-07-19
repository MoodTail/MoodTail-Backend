package com.example.moodtail.domain.image.service;

import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.domain.image.service.ImageService.StorageCleanupResult;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3StorageService storageService;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepository, storageService);
    }

    @Test
    void delegatesDistinctImagesToTheSharedStorageService() {
        String historyImage = "https://cdn.example/history.png";
        String sharedResultImage = "https://cdn.example/shared-result.png";
        StorageCleanupResult result = imageService.deleteImagesFromStorage(List.of(
                historyImage,
                sharedResultImage,
                historyImage
        ));

        verify(storageService).deleteImage(historyImage);
        verify(storageService).deleteImage(sharedResultImage);
        assertThat(result).isEqualTo(new StorageCleanupResult(2, 0));
    }

    @Test
    void continuesAfterSharedStorageFailureAndReportsIt() {
        String failedImage = "https://cdn.example/failed.png";
        String deletedImage = "https://cdn.example/deleted.png";
        doThrow(new S3StorageException("storage unavailable"))
                .when(storageService)
                .deleteImage(failedImage);

        StorageCleanupResult result = imageService.deleteImagesFromStorage(List.of(
                failedImage,
                deletedImage
        ));

        verify(storageService).deleteImage(deletedImage);
        assertThat(result).isEqualTo(new StorageCleanupResult(1, 1));
    }

    @Test
    void doesNotHideUnexpectedProgrammingFailureAsStorageCleanupFailure() {
        String failedImage = "https://cdn.example/failed.png";
        String deletedImage = "https://cdn.example/deleted.png";
        IllegalStateException programmingFailure = new IllegalStateException("unexpected failure");
        doThrow(programmingFailure)
                .when(storageService)
                .deleteImage(failedImage);

        assertThatThrownBy(() -> imageService.deleteImagesFromStorage(List.of(
                    failedImage,
                    deletedImage
                )))
                .isSameAs(programmingFailure);

        verify(storageService, never()).deleteImage(deletedImage);
    }
}
