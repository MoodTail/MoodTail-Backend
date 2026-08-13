package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.collection.entity.UserUnlockedCocktail;
import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.UserUnlockedCocktailRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionProgressService {

    private final CocktailRepository cocktailRepository;
    private final UserUnlockedCocktailRepository userUnlockedCocktailRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    public void collect(User user, List<Cocktail> cocktails) {
        Map<Long, Cocktail> distinctCocktails = cocktails.stream()
                .collect(Collectors.toMap(
                        Cocktail::getId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        if (distinctCocktails.isEmpty()) {
            return;
        }

        Set<Long> collectedCocktailIds = userUnlockedCocktailRepository.findUnlockedCocktailIds(
                user.getId(),
                distinctCocktails.keySet()
        );
        List<UserUnlockedCocktail> newCollections = distinctCocktails.values().stream()
                .filter(cocktail -> !collectedCocktailIds.contains(cocktail.getId()))
                .map(cocktail -> UserUnlockedCocktail.create(user, cocktail))
                .toList();
        if (!newCollections.isEmpty()) {
            userUnlockedCocktailRepository.saveAllAndFlush(newCollections);
        }

        distinctCocktails.values().stream()
                .map(Cocktail::getMoodType)
                .collect(Collectors.toMap(
                        MoodType::getId,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .forEach(moodType -> unlockIfThresholdReached(user, moodType));
    }

    private void unlockIfThresholdReached(User user, MoodType moodType) {
        if (userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(
                user.getId(),
                moodType.getId()
        )) {
            return;
        }

        long totalCocktailCount = cocktailRepository.countByMoodTypeId(moodType.getId());
        long collectedCocktailCount = userUnlockedCocktailRepository
                .countByUserIdAndCocktailMoodTypeId(user.getId(), moodType.getId());
        if (totalCocktailCount > 0 && collectedCocktailCount * 2 >= totalCocktailCount) {
            userUnlockedMoodTypeRepository.save(UserUnlockedMoodType.create(user, moodType));
        }
    }
}
