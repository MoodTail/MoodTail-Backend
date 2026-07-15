package com.example.moodtail.domain.history.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryPhotoServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-11T03:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate RECORD_DATE = LocalDate.of(2026, 7, 10);

    @Mock
    private HistoryPhotoRepository historyPhotoRepository;
    @Mock
    private HistoryImageWriter imageWriter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HistoryPhotoStorage photoStorage;
    @Mock
    private TransactionTemplate transactionTemplate;

    private HistoryPhotoService photoService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        photoService = new HistoryPhotoService(
                historyPhotoRepository,
                imageWriter,
                userRepository,
                photoStorage,
                transactionTemplate,
                CLOCK
        );
    }

    @Test
    void removesUploadedObjectWhenDatabasePersistenceFails() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/history/photos/photo.png";
        when(photoStorage.upload(file)).thenReturn(url);
        when(userRepository.getReferenceById(1L)).thenReturn(member());
        when(imageWriter.insert(url, ImageSourceType.GALLERY))
                .thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file, "GALLERY"))
                .isInstanceOf(RuntimeException.class);

        verify(photoStorage).delete(url);
    }

    @Test
    void keepsTheDatabaseFailureWhenUploadCompensationAlsoFails() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/history/photos/photo.png";
        IllegalStateException databaseFailure = new IllegalStateException("database failure");
        when(photoStorage.upload(file)).thenReturn(url);
        when(userRepository.getReferenceById(1L)).thenReturn(member());
        when(imageWriter.insert(url, ImageSourceType.GALLERY)).thenThrow(databaseFailure);
        org.mockito.Mockito.doThrow(new RestApiException(
                com.example.moodtail.global.common.exception.code.status.ImageErrorStatus.INVALID_IMAGE
        )).when(photoStorage).delete(url);

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file, "GALLERY"))
                .isSameAs(databaseFailure);
    }

    @Test
    void persistsUploadedPhoto() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/history/photos/photo.png";
        User user = member();
        Image image = mock(Image.class);
        when(photoStorage.upload(file)).thenReturn(url);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(imageWriter.insert(url, ImageSourceType.CAMERA)).thenReturn(image);
        when(historyPhotoRepository.save(any())).thenAnswer(invocation -> {
            HistoryPhoto photo = invocation.getArgument(0);
            ReflectionTestUtils.setField(photo, "id", 3L);
            return photo;
        });

        var response = photoService.add(1L, "2026-07-10", file, "camera");

        assertThat(response.photoId()).isEqualTo(3L);
        assertThat(response.recordDate()).isEqualTo(RECORD_DATE);
        assertThat(response.imageUrl()).isEqualTo(url);
        assertThat(response.sourceType()).isEqualTo(ImageSourceType.CAMERA);
    }

    @Test
    void mapsUploadStorageFailureToHistoryContract() {
        MultipartFile file = mock(MultipartFile.class);
        when(photoStorage.upload(file)).thenThrow(new S3StorageException("storage unavailable"));

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file, "camera"))
                .isInstanceOfSatisfying(
                        com.example.moodtail.global.common.exception.RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode())
                                .isEqualTo("HISTORY_PHOTO_503")
                );
    }

    @Test
    void deletesDatabaseRowsBeforeStoredObject() {
        Image image = mock(Image.class);
        when(image.getId()).thenReturn(9L);
        when(image.getImageUrl()).thenReturn("https://cdn.example/history/photos/photo.png");
        HistoryPhoto photo = HistoryPhoto.create(member(), RECORD_DATE, image);
        when(historyPhotoRepository.findOwnedForUpdate(3L, 1L, RECORD_DATE))
                .thenReturn(Optional.of(photo));
        when(imageWriter.deleteIfUnreferenced(9L)).thenReturn(true);

        photoService.delete(1L, "2026-07-10", 3L);

        verify(historyPhotoRepository).delete(photo);
        verify(historyPhotoRepository).flush();
        verify(imageWriter).deleteIfUnreferenced(9L);
        verify(photoStorage).delete(image.getImageUrl());
    }

    @Test
    void keepsStoredObjectWhenImageRowIsStillReferenced() {
        Image image = mock(Image.class);
        when(image.getId()).thenReturn(9L);
        when(image.getImageUrl()).thenReturn("https://cdn.example/history/photos/photo.png");
        HistoryPhoto photo = HistoryPhoto.create(member(), RECORD_DATE, image);
        when(historyPhotoRepository.findOwnedForUpdate(3L, 1L, RECORD_DATE))
                .thenReturn(Optional.of(photo));
        when(imageWriter.deleteIfUnreferenced(9L)).thenReturn(false);

        photoService.delete(1L, "2026-07-10", 3L);

        verify(photoStorage, never()).delete(image.getImageUrl());
    }

    @Test
    void completesDatabaseDeletionWhenStoredObjectDeletionFails() {
        Image image = mock(Image.class);
        String imageUrl = "https://cdn.example/history/photos/photo.png";
        when(image.getId()).thenReturn(9L);
        when(image.getImageUrl()).thenReturn(imageUrl);
        HistoryPhoto photo = HistoryPhoto.create(member(), RECORD_DATE, image);
        when(historyPhotoRepository.findOwnedForUpdate(3L, 1L, RECORD_DATE))
                .thenReturn(Optional.of(photo));
        when(imageWriter.deleteIfUnreferenced(9L)).thenReturn(true);
        org.mockito.Mockito.doThrow(new S3StorageException("storage unavailable"))
                .when(photoStorage)
                .delete(imageUrl);

        photoService.delete(1L, "2026-07-10", 3L);

        verify(historyPhotoRepository).delete(photo);
        verify(historyPhotoRepository).flush();
    }

    @Test
    void rejectsFutureDateBeforeUploadingImage() {
        MultipartFile file = mock(MultipartFile.class);

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-12", file, "CAMERA"))
                .isInstanceOfSatisfying(
                        com.example.moodtail.global.common.exception.RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400")
                );
    }

    private User member() {
        return User.createMember("회원", java.time.LocalDateTime.now(CLOCK));
    }
}
