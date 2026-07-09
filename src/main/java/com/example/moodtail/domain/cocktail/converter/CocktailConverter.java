package com.example.moodtail.domain.cocktail.converter;

import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CocktailConverter {

    public MoodTypeResponse toDetailResponse(MoodType moodType, MoodType bestMatch, MoodType worstMatch, List<Cocktail> cocktails) {
        return MoodTypeResponse.builder()
                .moodType(MoodTypeResponse.MoodTypeDto.builder()
                        .typeId(moodType.getId())
                        .typeCode(moodType.getCode())
                        .name(moodType.getName())
                        .description(moodType.getDescription())
                        .imageUrl("https://") // todo S3 연결 전 하드코딩
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
                                .imageUrl("https://...") //todo S3 연결 후 고치기
                                .build())
                        .toList())
                .build();
    }
}
