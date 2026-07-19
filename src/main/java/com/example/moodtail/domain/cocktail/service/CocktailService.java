package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailDetailResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteListResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailListResponse;
import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailFavorite;
import com.example.moodtail.domain.cocktail.repository.CocktailFavoriteRepository;
import com.example.moodtail.domain.cocktail.repository.DrinkingRecordRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedCocktailRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserStatus;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    private final UserUnlockedCocktailRepository userUnlockedCocktailRepository;
    private final DrinkingRecordRepository drinkingRecordRepository;

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
    public MoodTypeResponse getMoodType(
            Long moodTypeId,
            PrincipalDetails principalDetails
    ) {
        Long userId = principalDetails.getUserId();

        MoodType moodType = moodTypeRepository.findDetailById(moodTypeId)
                .orElseThrow(() -> new RestApiException(CocktailErrorStatus.COCKTAIL_TYPE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));

        boolean unlocked =
                userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(userId, moodTypeId);

        boolean representative =
                user.getRepresentativeMoodType() != null
                        && Objects.equals(
                        user.getRepresentativeMoodType().getId(),
                        moodTypeId
                );

        boolean canSetRepresentative = !user.isGuest() && unlocked && !representative;

        int typePercent = calculateRepresentativeTypePercent(userId, moodTypeId);

        List<MoodTypeCompatibility> compatibilityList =
                compatibilityRepository.findAllByMoodTypeId(moodTypeId);

        MoodType bestMatch = compatibilityList.stream()
                .filter(compatibility ->
                        compatibility.getCompatibilityType()
                                == CompatibilityType.BEST
                )
                .map(MoodTypeCompatibility::getTargetMoodType)
                .findFirst()
                .orElse(null);

        MoodType worstMatch = compatibilityList.stream()
                .filter(compatibility ->
                        compatibility.getCompatibilityType()
                                == CompatibilityType.WORST
                )
                .map(MoodTypeCompatibility::getTargetMoodType)
                .findFirst()
                .orElse(null);

        List<Cocktail> cocktails = cocktailRepository.findByMoodTypeId(moodTypeId);

        Set<Long> unlockedCocktailIds =
                userUnlockedCocktailRepository
                        .findUnlockedCocktailIds(
                                userId,
                                moodTypeId
                        );

        int totalCocktailCount = cocktails.size();
        int unlockedCocktailCount = unlockedCocktailIds.size();

        int collectionRate = calculateCollectionRate(
                unlockedCocktailCount,
                totalCocktailCount
        );

        List<MoodTypeResponse.CocktailSummaryDto> cocktailResponses =
                cocktails.stream()
                        .map(cocktail ->
                                MoodTypeResponse.CocktailSummaryDto.builder()
                                        .cocktailId(cocktail.getId())
                                        .nameKo(cocktail.getNameKo())
                                        .nameEn(cocktail.getNameEn())
                                        .shortDescription(
                                                cocktail.getShortDescription()
                                        )
                                        .imageUrl(
                                                getImageUrl(cocktail.getImage())
                                        )
                                        .unlocked(
                                                unlockedCocktailIds.contains(
                                                        cocktail.getId()
                                                )
                                        )
                                        .build()
                        )
                        .toList();

        return MoodTypeResponse.builder()
                .moodTypeId(moodType.getId())
                .typeCode(moodType.getCode())
                .name(moodType.getName())
                .shortDescription(moodType.getShortDescription())
                .description(moodType.getDescription())
                .catchphrase(moodType.getCharacterQuote())
                .characterImageUrl(
                        getImageUrl(moodType.getCharacterImage())
                )
                .unlocked(unlocked)
                .representative(representative)
                .canSetRepresentative(canSetRepresentative)
                .typePercent(typePercent)
                .collectionRate(collectionRate)
                .typeFigures(
                        MoodTypeResponse.TypeFiguresDto.builder()
                                .alcoholIntensity(convertFigure(moodType.getAlcoholIntensity()))
                                .sweetness(convertFigure(moodType.getSweetness()))
                                .sourness(convertFigure(moodType.getSourness()))
                                .refreshing(convertFigure(moodType.getRefreshing()))
                                .bitterness(convertFigure(moodType.getBitterness()))
                                .build()
                )
                .compatibilities(
                        MoodTypeResponse.CompatibilitiesDto.builder()
                                .best(toCompatibilityResponse(bestMatch))
                                .worst(toCompatibilityResponse(worstMatch))
                                .build()
                )
                .cocktails(cocktailResponses)
                .totalCocktailCount(totalCocktailCount)
                .unlockedCocktailCount(unlockedCocktailCount)
                .build();
    }

    private int convertFigure(BigDecimal value) {
        return value.multiply(
                new BigDecimal("20")
        ).intValue();
    }

    private int calculateCollectionRate(
            int unlockedCocktailCount,
            int totalCocktailCount
    ) {
        if (totalCocktailCount == 0) {
            return 0;
        }

        return (int) (
                unlockedCocktailCount * 100L
                        / totalCocktailCount
        );
    }

    private MoodTypeResponse.CompatibilityDto toCompatibilityResponse(
            MoodType moodType
    ) {
        if (moodType == null) {
            return null;
        }

        return MoodTypeResponse.CompatibilityDto.builder()
                .moodTypeId(moodType.getId())
                .typeCode(moodType.getCode())
                .name(moodType.getName())
                .characterImageUrl(
                        getImageUrl(moodType.getCharacterImage())
                )
                .build();
    }


    private int calculateRepresentativeTypePercent(Long userId, Long moodTypeId) {
        long totalUserCount =
                drinkingRecordRepository.countByUser_Id(userId);

        if (totalUserCount == 0) {
            return 0;
        }

        long typeRecordCount = drinkingRecordRepository.countByUser_IdAndCocktail_MoodType_Id(userId, moodTypeId);

        return (int) Math.round(
                typeRecordCount * 100.0 / totalUserCount
        );
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

        try {
            cocktailFavoriteRepository.save(CocktailFavorite.create(user, cocktail));
        } catch (DataIntegrityViolationException e) {
            throw new RestApiException(CocktailErrorStatus.COCKTAIL_FAVORITE_ALREADY_EXISTS);
        }

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
