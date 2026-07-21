package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationShareRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResponse;
import com.example.moodtail.domain.recommendation.entity.SharedPairRecommendation;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.recommendation.repository.SharedPairRecommendationRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.ShareErrorStatus.SHARE_COCKTAIL_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.ShareErrorStatus.SHARE_INVALID_RECOMMENDATIONS_SIZE;

@Service
@RequiredArgsConstructor
public class PairRecommendationShareService {

    private static final String SHARE_PATH = "/share/pair/";
    private static final int TOKEN_BYTE_LENGTH = 18;
    private static final int REQUIRED_RECOMMENDATIONS_SIZE = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final CocktailRepository cocktailRepository;
    private final SharedPairRecommendationRepository sharedPairRecommendationRepository;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    @Transactional
    public PairRecommendationShareResponse createShare(Long userId, PairRecommendationShareRequest request) {
        validateRecommendationsSize(request.recommendations());
        validateCocktailsExist(request.recommendations());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        TasteProfile compromiseProfile = toTasteProfile(request.compromiseProfile());
        List<RecommendationItemCommand> recommendations = toCommands(request.recommendations());
        String shareToken = generateShareToken();

        SharedPairRecommendation sharedPairRecommendation = SharedPairRecommendation.create(
                creator,
                shareToken,
                compromiseProfile,
                recommendations,
                request.myMatchScore(),
                request.partnerMatchScore(),
                request.thumbnailImageUrl()
        );
        sharedPairRecommendationRepository.save(sharedPairRecommendation);

        return new PairRecommendationShareResponse(
                shareToken,
                normalizeBaseUrl(shareBaseUrl) + SHARE_PATH + shareToken
        );
    }

    private void validateRecommendationsSize(
            List<PairRecommendationShareRequest.RecommendationDto> recommendations
    ) {
        if (recommendations == null || recommendations.size() != REQUIRED_RECOMMENDATIONS_SIZE) {
            throw new RestApiException(SHARE_INVALID_RECOMMENDATIONS_SIZE);
        }
    }

    private void validateCocktailsExist(
            List<PairRecommendationShareRequest.RecommendationDto> recommendations
    ) {
        Set<Long> requestedCocktailIds = recommendations.stream()
                .map(PairRecommendationShareRequest.RecommendationDto::cocktailId)
                .collect(Collectors.toSet());

        Set<Long> existingCocktailIds = cocktailRepository.findAllById(requestedCocktailIds).stream()
                .map(Cocktail::getId)
                .collect(Collectors.toSet());

        if (!existingCocktailIds.containsAll(requestedCocktailIds)) {
            throw new RestApiException(SHARE_COCKTAIL_NOT_FOUND);
        }
    }

    private TasteProfile toTasteProfile(PairRecommendationShareRequest.CompromiseProfileDto profile) {
        return TasteProfile.of(
                profile.alcoholIntensity(),
                profile.sweetness(),
                profile.sourness(),
                profile.refreshing(),
                profile.bitterness()
        );
    }

    private List<RecommendationItemCommand> toCommands(
            List<PairRecommendationShareRequest.RecommendationDto> recommendations
    ) {
        return recommendations.stream()
                .sorted(Comparator.comparingInt(PairRecommendationShareRequest.RecommendationDto::ranking))
                .map(r -> new RecommendationItemCommand(r.cocktailId(), r.matchScore()))
                .toList();
    }

    private String generateShareToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return "p_" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
