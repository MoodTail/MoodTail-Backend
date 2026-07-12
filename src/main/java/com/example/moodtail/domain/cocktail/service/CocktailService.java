package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailDetailResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteListResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailListResponse;
import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailFavorite;
import com.example.moodtail.domain.cocktail.repository.CocktailFavoriteRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CocktailService {
    private static final BigDecimal MIN_ALCOHOL_DEGREE = BigDecimal.ZERO;

    private final MoodTypeRepository moodTypeRepository;
    private final MoodTypeCompatibilityRepository compatibilityRepository;
    private final CocktailRepository cocktailRepository;
    private final CocktailFavoriteRepository cocktailFavoriteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CocktailListResponse getCocktails(
            BigDecimal minAlcoholDegree,
            BigDecimal maxAlcoholDegree,
            String keyword,
            PrincipalDetails principalDetails
    ) {
        validateAlcoholDegreeRange(minAlcoholDegree, maxAlcoholDegree);

        List<Cocktail> cocktails = cocktailRepository.searchCocktails(
                minAlcoholDegree,
                maxAlcoholDegree,
                normalizeKeyword(keyword)
        );
        Set<Long> favoriteCocktailIds = getFavoriteCocktailIds(cocktails, principalDetails);

        List<CocktailListResponse.CocktailSummaryDto> cocktailSummaries = cocktails.stream()
                .map(cocktail -> CocktailListResponse.CocktailSummaryDto.from(
                        cocktail,
                        getIsFavorite(cocktail.getId(), favoriteCocktailIds)
                ))
                .toList();

        return CocktailListResponse.builder()
                .cocktails(cocktailSummaries)
                .build();
    }

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

    @Transactional(readOnly = true)
    public CocktailDetailResponse getCocktailDetail(Long cocktailId, PrincipalDetails principalDetails) {
        Cocktail cocktail = cocktailRepository.findDetailWithIngredientsById(cocktailId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_NOT_FOUND));
        // 같은 트랜잭션 내 동일 ID 조회이므로 영속성 컨텍스트가 위 cocktail 인스턴스에 recipeSteps만 채워서 반환한다.
        cocktail = cocktailRepository.findDetailWithRecipeStepsById(cocktailId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_NOT_FOUND));

        boolean isFavorite = principalDetails != null
                && cocktailFavoriteRepository.existsByUserIdAndCocktailId(principalDetails.getUserId(), cocktailId);

        return CocktailDetailResponse.from(cocktail, isFavorite, getImageUrl(cocktail.getImage()));
    }

    @Transactional
    public CocktailFavoriteResponse addFavorite(Long cocktailId, PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUserId();

        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_NOT_FOUND));

        if (cocktailFavoriteRepository.existsByUserIdAndCocktailId(userId, cocktailId)) {
            throw new RestApiException(CocktailErrorStatus.COCKTAIL_FAVORITE_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));

        cocktailFavoriteRepository.save(CocktailFavorite.create(user, cocktail));

        return CocktailFavoriteResponse.from(cocktail);
    }

    @Transactional
    public CocktailFavoriteResponse removeFavorite(Long cocktailId, PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUserId();

        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_NOT_FOUND));

        CocktailFavorite favorite = cocktailFavoriteRepository.findByUserIdAndCocktailId(userId, cocktailId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_FAVORITE_NOT_FOUND));

        cocktailFavoriteRepository.delete(favorite);

        return CocktailFavoriteResponse.from(cocktail);
    }

    @Transactional(readOnly = true)
    public CocktailFavoriteListResponse getFavoriteCocktails(PrincipalDetails principalDetails) {
        List<Cocktail> cocktails = cocktailFavoriteRepository.findFavoriteCocktailsByUserId(principalDetails.getUserId());

        List<CocktailFavoriteListResponse.CocktailFavoriteSummaryDto> cocktailSummaries = cocktails.stream()
                .map(CocktailFavoriteListResponse.CocktailFavoriteSummaryDto::from)
                .toList();

        return CocktailFavoriteListResponse.builder()
                .cocktails(cocktailSummaries)
                .build();
    }

    private String getImageUrl(Image image) {
        return image == null ? null : image.getImageUrl();
    }

    private void validateAlcoholDegreeRange(BigDecimal minAlcoholDegree, BigDecimal maxAlcoholDegree) {
        boolean hasNegativeDegree = (minAlcoholDegree != null && minAlcoholDegree.compareTo(MIN_ALCOHOL_DEGREE) < 0)
                || (maxAlcoholDegree != null && maxAlcoholDegree.compareTo(MIN_ALCOHOL_DEGREE) < 0);
        boolean isReversedRange = minAlcoholDegree != null
                && maxAlcoholDegree != null
                && minAlcoholDegree.compareTo(maxAlcoholDegree) > 0;

        if (hasNegativeDegree || isReversedRange) {
            throw new RestApiException(CocktailErrorStatus.INVALID_ALCOHOL_DEGREE_RANGE);
        }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private Set<Long> getFavoriteCocktailIds(
            List<Cocktail> cocktails,
            PrincipalDetails principalDetails
    ) {
        if (principalDetails == null) {
            return Set.of();
        }
        if (cocktails.isEmpty()) {
            return Set.of();
        }

        List<Long> cocktailIds = cocktails.stream()
                .map(Cocktail::getId)
                .toList();
        return new HashSet<>(cocktailFavoriteRepository.findFavoriteCocktailIds(
                principalDetails.getUserId(),
                cocktailIds
        ));
    }

    private boolean getIsFavorite(Long cocktailId, Set<Long> favoriteCocktailIds) {
        return favoriteCocktailIds.contains(cocktailId);
    }
}
