package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.moodtest.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CocktailService {
    private final MoodTypeRepository moodTypeRepository;
    private final MoodTypeCompatibilityRepository compatibilityRepository;
    private final CocktailRepository cocktailRepository;

    @Transactional(readOnly = true)
    public MoodTypeResponse getMoodType(Long typeId) {
        MoodType moodType = moodTypeRepository.findById(typeId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_TYPE_NOT_FOUND));

        String characterImageUrl = getImageUrl(moodType.getCharacterImage());

        MoodType bestMatch = compatibilityRepository.findByMoodTypeAndCompatibilityType(moodType, CompatibilityType.BEST)
                .map(MoodTypeCompatibility::getTargetMoodType)
                .orElse(null);

        MoodType worstMatch = compatibilityRepository.findByMoodTypeAndCompatibilityType(moodType, CompatibilityType.WORST)
                .map(MoodTypeCompatibility::getTargetMoodType)
                .orElse(null);

        List<Cocktail> cocktails = cocktailRepository.findByMoodTypeId(typeId);

        return MoodTypeResponse.builder()
                .moodType(MoodTypeResponse.MoodTypeDto.builder()
                        .typeId(moodType.getId())
                        .typeCode(moodType.getCode())
                        .name(moodType.getName())
                        .description(moodType.getDescription())
                        .imageUrl(characterImageUrl) // todo S3 연결 전 하드코딩
                        .typePercent(68)      // todo 기능 구현 전 임시 하드코딩
                        .build())
                .typeFigures(MoodTypeResponse.TypeFiguresDto.builder()
                        .alcoholIntensity(moodType.getAlcoholIntensity().multiply(new BigDecimal("20")).intValue())
                        .sweetness(moodType.getSweetness().multiply(new BigDecimal("20")).intValue())
                        .sourness(moodType.getSourness().multiply(new BigDecimal("20")).intValue())
                        .bitterness(moodType.getBitterness().multiply(new BigDecimal("20")).intValue())
                        .refreshing(moodType.getRefreshing().multiply(new BigDecimal("20")).intValue())
                        .build())
                .bestMatchType(bestMatch != null ? MoodTypeResponse.MatchTypeDto.builder()
                        .typeId(bestMatch.getId())
                        .name(bestMatch.getName())
                        .build() : null)
                .worstMatchType(worstMatch != null ? MoodTypeResponse.MatchTypeDto.builder()
                        .typeId(worstMatch.getId())
                        .name(worstMatch.getName())
                        .build() : null)
                .cocktails(cocktails.stream()
                        .map(c -> MoodTypeResponse.CocktailSummaryDto.builder()
                                .cocktailId(c.getId())
                                .name(c.getNameKo())
                                .shortDescription(c.getShortDescription())
                                .imageUrl(getImageUrl(c.getImage()))
                                .build())
                        .toList())
                .build();
    }

    private String getImageUrl(Image image) {
        return image == null ? null : image.getImageUrl();
    }
}
