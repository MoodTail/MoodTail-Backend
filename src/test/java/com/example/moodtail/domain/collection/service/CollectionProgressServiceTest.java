package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.collection.entity.UserUnlockedCocktail;
import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.UserUnlockedCocktailRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionProgressServiceTest {

    @Mock
    private CocktailRepository cocktailRepository;
    @Mock
    private UserUnlockedCocktailRepository userUnlockedCocktailRepository;
    @Mock
    private UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    private CollectionProgressService service;

    @BeforeEach
    void setUp() {
        service = new CollectionProgressService(
                cocktailRepository,
                userUnlockedCocktailRepository,
                userUnlockedMoodTypeRepository
        );
    }

    @Test
    void collectsDistinctCocktailAndUnlocksMoodTypeAtFiftyPercent() {
        User user = user(1L);
        MoodType moodType = moodType(10L);
        Cocktail cocktail = cocktail(100L, moodType);
        when(userUnlockedCocktailRepository.findUnlockedCocktailIds(1L, Set.of(100L)))
                .thenReturn(Set.of());
        when(cocktailRepository.countByMoodTypeId(10L)).thenReturn(8L);
        when(userUnlockedCocktailRepository.countByUserIdAndCocktailMoodTypeId(1L, 10L))
                .thenReturn(4L);

        service.collect(user, List.of(cocktail, cocktail));

        ArgumentCaptor<List<UserUnlockedCocktail>> cocktailCaptor = ArgumentCaptor.forClass(List.class);
        verify(userUnlockedCocktailRepository).saveAllAndFlush(cocktailCaptor.capture());
        assertThat(cocktailCaptor.getValue()).singleElement().satisfies(unlockedCocktail -> {
            assertThat(unlockedCocktail.getUser()).isSameAs(user);
            assertThat(unlockedCocktail.getCocktail()).isSameAs(cocktail);
        });
        ArgumentCaptor<UserUnlockedMoodType> moodTypeCaptor =
                ArgumentCaptor.forClass(UserUnlockedMoodType.class);
        verify(userUnlockedMoodTypeRepository).save(moodTypeCaptor.capture());
        assertThat(moodTypeCaptor.getValue().getMoodType()).isSameAs(moodType);
    }

    @Test
    void doesNotUnlockMoodTypeBelowFiftyPercent() {
        User user = user(1L);
        MoodType moodType = moodType(10L);
        Cocktail cocktail = cocktail(100L, moodType);
        when(userUnlockedCocktailRepository.findUnlockedCocktailIds(1L, Set.of(100L)))
                .thenReturn(Set.of());
        when(cocktailRepository.countByMoodTypeId(10L)).thenReturn(8L);
        when(userUnlockedCocktailRepository.countByUserIdAndCocktailMoodTypeId(1L, 10L))
                .thenReturn(3L);

        service.collect(user, List.of(cocktail));

        verify(userUnlockedMoodTypeRepository, never()).save(any());
    }

    @Test
    void preservesExistingCollectionAndPermanentMoodTypeUnlock() {
        User user = user(1L);
        MoodType moodType = moodType(10L);
        Cocktail cocktail = cocktail(100L, moodType);
        when(userUnlockedCocktailRepository.findUnlockedCocktailIds(1L, Set.of(100L)))
                .thenReturn(Set.of(100L));
        when(userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(1L, 10L))
                .thenReturn(true);

        service.collect(user, List.of(cocktail));

        verify(userUnlockedCocktailRepository, never()).saveAllAndFlush(any());
        verify(cocktailRepository, never()).countByMoodTypeId(any());
        verify(userUnlockedMoodTypeRepository, never()).save(any());
    }

    private User user(Long id) {
        User user = User.createMember("사용자", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodType moodType(Long id) {
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        when(moodType.getId()).thenReturn(id);
        return moodType;
    }

    private Cocktail cocktail(Long id, MoodType moodType) {
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(cocktail.getId()).thenReturn(id);
        when(cocktail.getMoodType()).thenReturn(moodType);
        return cocktail;
    }
}
