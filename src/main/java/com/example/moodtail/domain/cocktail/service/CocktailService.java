package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.converter.CocktailConverter;
import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.moodtest.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CocktailService {
    private final MoodTypeRepository moodTypeRepository;
    private final MoodTypeCompatibilityRepository compatibilityRepository;
    private final CocktailRepository cocktailRepository;
    private final CocktailConverter cocktailConverter;

    @Transactional(readOnly = true)
    public MoodTypeResponse getMoodType(Long typeId) {
        MoodType moodType = moodTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 칵테일 타입을 찾을 수 없습니다. typeId=" + typeId));


        MoodType bestMatch = compatibilityRepository.findByMoodTypeAndCompatibilityType(moodType, CompatibilityType.BEST)
                .map(MoodTypeCompatibility::getTargetMoodType)
                .orElse(null);

        MoodType worstMatch = compatibilityRepository.findByMoodTypeAndCompatibilityType(moodType, CompatibilityType.WORST)
                .map(MoodTypeCompatibility::getTargetMoodType)
                .orElse(null);

        List<Cocktail> cocktails = cocktailRepository.findByMoodTypeId(typeId);

        return cocktailConverter.toDetailResponse(moodType, bestMatch, worstMatch, cocktails);
    }
}
