package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.MoodTypesResponse;
import com.example.moodtail.domain.collection.repository.MoodTypeCollectionProjection;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UnlockedMoodTypeServiceTest {

    @Mock
    private UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    @InjectMocks
    private MoodTypeCollectionService moodTypeCollectionService;

    @Test
    void getMoodTypesIncludingLockedMoodTypes() {
        LocalDateTime unlockedAt = LocalDateTime.of(2026, 7, 10, 18, 38, 51);
        MoodTypeCollectionProjection unlocked = projection(2002L, "TYPE_1", unlockedAt);
        MoodTypeCollectionProjection locked = projection(2003L, "TYPE_2", null);
        given(userUnlockedMoodTypeRepository.findAllMoodTypesByUserId(1L))
                .willReturn(List.of(unlocked, locked));

        MoodTypesResponse response = moodTypeCollectionService.getMoodTypes(
                1L,
                UserRole.USER.name()
        );

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.moodTypes().get(0).unlockedAt()).isEqualTo(unlockedAt);
        assertThat(response.moodTypes().get(1).unlockedAt()).isNull();
    }

    @Test
    void returnEmptyListWhenMoodTypesDoNotExist() {
        given(userUnlockedMoodTypeRepository.findAllMoodTypesByUserId(1L)).willReturn(List.of());

        MoodTypesResponse response = moodTypeCollectionService.getMoodTypes(
                1L,
                UserRole.USER.name()
        );

        assertThat(response.totalCount()).isZero();
        assertThat(response.moodTypes()).isEmpty();
    }

    @Test
    void guestCannotGetMoodTypes() {
        assertThatThrownBy(() -> moodTypeCollectionService.getMoodTypes(
                1L,
                UserRole.GUEST.name()
        ))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH027")
                );

        verifyNoInteractions(userUnlockedMoodTypeRepository);
    }

    private MoodTypeCollectionProjection projection(Long id, String code, LocalDateTime unlockedAt) {
        MoodTypeCollectionProjection projection = mock(MoodTypeCollectionProjection.class);
        given(projection.getMoodTypeId()).willReturn(id);
        given(projection.getTypeCode()).willReturn(code);
        given(projection.getName()).willReturn("mood type");
        given(projection.getShortDescription()).willReturn("description");
        given(projection.getCharacterImageUrl()).willReturn("https://cdn.moodtail.com/types/type.png");
        given(projection.getUnlockedAt()).willReturn(unlockedAt);
        return projection;
    }
}
