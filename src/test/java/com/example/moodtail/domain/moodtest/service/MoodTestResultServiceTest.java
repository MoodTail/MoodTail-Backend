package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.moodtest.repository.MoodQuestionOptionRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteProfileCalculator;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class MoodTestResultServiceTest {

    @Mock
    private MoodQuestionOptionRepository moodQuestionOptionRepository;
    @Mock
    private MoodTypeRepository moodTypeRepository;
    @Mock
    private CocktailRepository cocktailRepository;
    @Mock
    private MoodTypeCompatibilityRepository moodTypeCompatibilityRepository;
    @Mock
    private TasteProfileCalculator tasteProfileCalculator;
    @Mock
    private TasteSimilarityCalculator tasteSimilarityCalculator;

    private MoodTestResultService moodTestResultService;

    @BeforeEach
    void setUp() {
        moodTestResultService = new MoodTestResultService(
                moodQuestionOptionRepository,
                moodTypeRepository,
                cocktailRepository,
                moodTypeCompatibilityRepository,
                tasteProfileCalculator,
                tasteSimilarityCalculator
        );
    }

    @Test
    void calculatesMoodTypeFourRecommendationsAndCompatibilities() {
        TasteProfile userTaste = taste("2.0", "4.0", "3.0", "3.0", "2.0");
        TasteProfile matchedTaste = taste("2.0", "4.0", "3.0", "3.0", "2.0");
        TasteProfile otherTaste = taste("5.0", "1.0", "1.0", "1.0", "5.0");
        MoodType matched = moodType(2001L, "TYPE01", "낭만파", matchedTaste, 1);
        MoodType other = moodType(2002L, "TYPE02", "모험가", otherTaste, 2);
        MoodType best = moodType(2003L, "TYPE03", "친구", matchedTaste, 3);
        MoodType worst = moodType(2004L, "TYPE04", "반대", otherTaste, 4);
        when(moodTypeRepository.findAllByOrderBySortOrderAscIdAsc())
                .thenReturn(List.of(matched, other));
        when(tasteSimilarityCalculator.calculateDistance(userTaste, matchedTaste)).thenReturn(0.0);
        when(tasteSimilarityCalculator.calculateDistance(userTaste, otherTaste)).thenReturn(5.0);

        TasteProfile firstTaste = taste("2.0", "4.0", "3.0", "3.0", "2.1");
        TasteProfile secondTaste = taste("2.0", "4.0", "3.0", "3.0", "2.2");
        TasteProfile thirdTaste = taste("2.0", "4.0", "3.0", "3.0", "2.3");
        TasteProfile fourthTaste = taste("2.0", "4.0", "3.0", "3.0", "2.4");
        Cocktail first = cocktail(101L, "첫 번째", firstTaste);
        Cocktail second = cocktail(102L, "두 번째", secondTaste);
        Cocktail third = cocktail(103L, "세 번째", thirdTaste);
        Cocktail fourth = cocktail(104L, "네 번째", fourthTaste);
        when(cocktailRepository.findByMoodTypeId(2001L))
                .thenReturn(List.of(fourth, second, first, third));
        when(tasteSimilarityCalculator.calculateDistance(userTaste, firstTaste)).thenReturn(0.1);
        when(tasteSimilarityCalculator.calculateDistance(userTaste, secondTaste)).thenReturn(0.2);
        when(tasteSimilarityCalculator.calculateDistance(userTaste, thirdTaste)).thenReturn(0.3);
        when(tasteSimilarityCalculator.calculateDistance(userTaste, fourthTaste)).thenReturn(0.4);
        when(tasteSimilarityCalculator.calculateMatchScore(0.1)).thenReturn(99);
        when(tasteSimilarityCalculator.calculateMatchScore(0.2)).thenReturn(98);
        when(tasteSimilarityCalculator.calculateMatchScore(0.3)).thenReturn(97);
        when(tasteSimilarityCalculator.calculateMatchScore(0.4)).thenReturn(96);
        MoodTypeCompatibility bestCompatibility = compatibility(best);
        MoodTypeCompatibility worstCompatibility = compatibility(worst);
        when(moodTypeCompatibilityRepository.findByMoodTypeIdAndCompatibilityType(
                2001L, CompatibilityType.BEST
        )).thenReturn(Optional.of(bestCompatibility));
        when(moodTypeCompatibilityRepository.findByMoodTypeIdAndCompatibilityType(
                2001L, CompatibilityType.WORST
        )).thenReturn(Optional.of(worstCompatibility));

        MoodTestResultResponse response = moodTestResultService.calculateResult(userTaste);

        assertThat(response.resultId()).isNull();
        assertThat(response.saved()).isFalse();
        assertThat(response.moodType().moodTypeId()).isEqualTo(2001L);
        assertThat(response.recommendations())
                .extracting(MoodTestResultResponse.RecommendationDto::cocktailId)
                .containsExactly(101L, 102L, 103L, 104L);
        assertThat(response.recommendations())
                .extracting(MoodTestResultResponse.RecommendationDto::ranking)
                .containsExactly(1, 2, 3, 4);
        assertThat(response.compatibilities().best().moodTypeId()).isEqualTo(2003L);
        assertThat(response.compatibilities().worst().moodTypeId()).isEqualTo(2004L);
    }

    @Test
    void rejectsResultRequestWhenAnswerCountIsNotSeven() {
        MoodTestResultRequest request = new MoodTestResultRequest(List.of(
                new MoodTestResultRequest.AnswerDto(1L, 1L)
        ));

        assertThatThrownBy(() -> moodTestResultService.calculateResult(request))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("MOOD_TEST400")
                );

        verifyNoInteractions(moodQuestionOptionRepository, moodTypeRepository, cocktailRepository);
    }

    private MoodType moodType(Long id, String code, String name, TasteProfile taste, int sortOrder) {
        MoodType moodType = mock(MoodType.class, withSettings().lenient());
        when(moodType.getId()).thenReturn(id);
        when(moodType.getCode()).thenReturn(code);
        when(moodType.getName()).thenReturn(name);
        when(moodType.getShortDescription()).thenReturn(name + " 설명");
        when(moodType.getCharacterQuote()).thenReturn(name + " 한마디");
        when(moodType.getSortOrder()).thenReturn(sortOrder);
        when(moodType.toTasteProfile()).thenReturn(taste);
        return moodType;
    }

    private Cocktail cocktail(Long id, String name, TasteProfile taste) {
        Cocktail cocktail = mock(Cocktail.class, withSettings().lenient());
        when(cocktail.getId()).thenReturn(id);
        when(cocktail.getNameKo()).thenReturn(name);
        when(cocktail.getNameEn()).thenReturn("Cocktail " + id);
        when(cocktail.getShortDescription()).thenReturn(name + " 설명");
        when(cocktail.toTasteProfile()).thenReturn(taste);
        return cocktail;
    }

    private MoodTypeCompatibility compatibility(MoodType target) {
        MoodTypeCompatibility compatibility = mock(
                MoodTypeCompatibility.class,
                withSettings().lenient()
        );
        when(compatibility.getTargetMoodType()).thenReturn(target);
        return compatibility;
    }

    private TasteProfile taste(
            String alcohol,
            String sweetness,
            String sourness,
            String refreshing,
            String bitterness
    ) {
        return TasteProfile.of(
                new BigDecimal(alcohol),
                new BigDecimal(sweetness),
                new BigDecimal(sourness),
                new BigDecimal(refreshing),
                new BigDecimal(bitterness)
        );
    }
}
