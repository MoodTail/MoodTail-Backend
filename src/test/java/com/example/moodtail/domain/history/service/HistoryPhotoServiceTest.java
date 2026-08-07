package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.history.entity.HistoryPhoto;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.entity.ImageSourceType;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private ImageRepository imageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private S3StorageService storageService;
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
                imageRepository,
                userRepository,
                storageService,
                transactionTemplate,
                CLOCK
        );
    }

    @Test
    void removesUploadedObjectWhenDatabasePersistenceFails() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/public/history/photos/photo.png";
        when(storageService.uploadImage(file, "public/history/photos")).thenReturn(url);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(member()));
        when(imageRepository.save(any(Image.class)))
                .thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file))
                .isInstanceOf(RuntimeException.class);

        verify(storageService).deleteImage(url);
    }

    @Test
    void keepsTheDatabaseFailureWhenUploadCompensationAlsoFails() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/public/history/photos/photo.png";
        IllegalStateException databaseFailure = new IllegalStateException("database failure");
        when(storageService.uploadImage(file, "public/history/photos")).thenReturn(url);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(member()));
        when(imageRepository.save(any(Image.class))).thenThrow(databaseFailure);
        org.mockito.Mockito.doThrow(new S3StorageException("storage failure"))
                .when(storageService)
                .deleteImage(url);

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file))
                .isSameAs(databaseFailure);
    }

    @Test
    void persistsUploadedPhotoWithPermanentPublicUrl() {
        MultipartFile file = mock(MultipartFile.class);
        String storedUrl = "https://cdn.example/public/history/photos/photo.png";
        User user = member();
        when(storageService.uploadImage(file, "public/history/photos")).thenReturn(storedUrl);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyPhotoRepository.save(any())).thenAnswer(invocation -> {
            HistoryPhoto photo = invocation.getArgument(0);
            ReflectionTestUtils.setField(photo, "id", 3L);
            return photo;
        });

        var response = photoService.add(1L, "2026-07-10", file);

        assertThat(response.photoId()).isEqualTo(3L);
        assertThat(response.recordDate()).isEqualTo(RECORD_DATE);
        assertThat(response.imageUrl()).isEqualTo(storedUrl);

        ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
        verify(imageRepository).save(imageCaptor.capture());
        assertThat(imageCaptor.getValue().getImageUrl()).isEqualTo(storedUrl);
        assertThat(imageCaptor.getValue().getSourceType()).isEqualTo(ImageSourceType.GALLERY);
        InOrder persistenceOrder = inOrder(historyPhotoRepository, storageService, userRepository);
        persistenceOrder.verify(historyPhotoRepository).countByUserIdAndRecordDate(1L, RECORD_DATE);
        persistenceOrder.verify(storageService).uploadImage(file, "public/history/photos");
        persistenceOrder.verify(userRepository).findByIdForUpdate(1L);
        persistenceOrder.verify(historyPhotoRepository).countByUserIdAndRecordDate(1L, RECORD_DATE);
        verify(historyPhotoRepository, times(2)).countByUserIdAndRecordDate(1L, RECORD_DATE);
    }

    @Test
    void rejectsSixthPhotoBeforeUploadingImage() {
        MultipartFile file = mock(MultipartFile.class);
        when(historyPhotoRepository.countByUserIdAndRecordDate(1L, RECORD_DATE)).thenReturn(5L);

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode())
                                .isEqualTo("HISTORY_PHOTO409")
                );

        verify(storageService, never()).uploadImage(any(), anyString());
        verify(imageRepository, never()).save(any(Image.class));
        verify(historyPhotoRepository, never()).save(any(HistoryPhoto.class));
    }

    @Test
    void removesUploadedObjectWhenConcurrentRequestFillsTheFifthSlot() {
        MultipartFile file = mock(MultipartFile.class);
        String url = "https://cdn.example/public/history/photos/photo.png";
        when(historyPhotoRepository.countByUserIdAndRecordDate(1L, RECORD_DATE))
                .thenReturn(4L, 5L);
        when(storageService.uploadImage(file, "public/history/photos")).thenReturn(url);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(member()));

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode())
                                .isEqualTo("HISTORY_PHOTO409")
                );

        verify(userRepository).findByIdForUpdate(1L);
        verify(storageService).deleteImage(url);
        verify(imageRepository, never()).save(any(Image.class));
        verify(historyPhotoRepository, never()).save(any(HistoryPhoto.class));
    }

    @Test
    void mapsUploadStorageFailureToHistoryContract() {
        MultipartFile file = mock(MultipartFile.class);
        when(storageService.uploadImage(file, "public/history/photos"))
                .thenThrow(new S3StorageException("storage unavailable"));

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-10", file))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode())
                                .isEqualTo("HISTORY_PHOTO503")
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
        when(historyPhotoRepository.existsByImageId(9L)).thenReturn(false);

        photoService.delete(1L, "2026-07-10", 3L);

        InOrder deletionOrder = inOrder(historyPhotoRepository, imageRepository, storageService);
        deletionOrder.verify(historyPhotoRepository).delete(photo);
        deletionOrder.verify(historyPhotoRepository).flush();
        deletionOrder.verify(historyPhotoRepository).existsByImageId(9L);
        deletionOrder.verify(imageRepository).delete(image);
        deletionOrder.verify(imageRepository).flush();
        deletionOrder.verify(storageService).deleteImage(image.getImageUrl());
    }

    @Test
    void keepsStoredObjectWhenImageRowIsStillReferenced() {
        Image image = mock(Image.class);
        when(image.getId()).thenReturn(9L);
        when(image.getImageUrl()).thenReturn("https://cdn.example/history/photos/photo.png");
        HistoryPhoto photo = HistoryPhoto.create(member(), RECORD_DATE, image);
        when(historyPhotoRepository.findOwnedForUpdate(3L, 1L, RECORD_DATE))
                .thenReturn(Optional.of(photo));
        when(historyPhotoRepository.existsByImageId(9L)).thenReturn(true);

        photoService.delete(1L, "2026-07-10", 3L);

        verify(imageRepository, never()).delete(any(Image.class));
        verify(storageService, never()).deleteImage(image.getImageUrl());
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
        when(historyPhotoRepository.existsByImageId(9L)).thenReturn(false);
        org.mockito.Mockito.doThrow(new S3StorageException("storage unavailable"))
                .when(storageService)
                .deleteImage(imageUrl);

        photoService.delete(1L, "2026-07-10", 3L);

        verify(historyPhotoRepository).delete(photo);
        verify(historyPhotoRepository).flush();
    }

    @Test
    void doesNotDeleteStoredObjectWhenImageDeletionFails() {
        Image image = mock(Image.class);
        String imageUrl = "https://cdn.example/history/photos/photo.png";
        when(image.getId()).thenReturn(9L);
        when(image.getImageUrl()).thenReturn(imageUrl);
        HistoryPhoto photo = HistoryPhoto.create(member(), RECORD_DATE, image);
        when(historyPhotoRepository.findOwnedForUpdate(3L, 1L, RECORD_DATE))
                .thenReturn(Optional.of(photo));
        when(historyPhotoRepository.existsByImageId(9L)).thenReturn(false);
        IllegalStateException databaseFailure = new IllegalStateException("database failure");
        org.mockito.Mockito.doThrow(databaseFailure).when(imageRepository).flush();

        assertThatThrownBy(() -> photoService.delete(1L, "2026-07-10", 3L))
                .isSameAs(databaseFailure);

        verify(storageService, never()).deleteImage(imageUrl);
    }

    @Test
    void rejectsFutureDateBeforeUploadingImage() {
        MultipartFile file = mock(MultipartFile.class);

        assertThatThrownBy(() -> photoService.add(1L, "2026-07-12", file))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY400")
                );

        verify(storageService, never()).uploadImage(any(), anyString());
    }

    private User member() {
        return User.createMember("회원", java.time.LocalDateTime.now(CLOCK));
    }
}
