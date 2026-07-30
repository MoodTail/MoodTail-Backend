package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.recommendation.dto.response.CompromiseProfileResponse;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResultResponse;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationSharePageResponse;
import com.example.moodtail.domain.recommendation.dto.response.SharedRecommendedCocktailResponse;
import com.example.moodtail.domain.recommendation.entity.SharedPairRecommendation;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.recommendation.repository.SharedPairRecommendationRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.ShareErrorStatus.SHARE_TOKEN_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PairRecommendationShareQueryService {

    private static final String SHARE_PATH = "/share/pair/";

    private final SharedPairRecommendationRepository sharedPairRecommendationRepository;
    private final CocktailRepository cocktailRepository;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    @Value("${app.share.frontend-base-url}")
    private String shareFrontendBaseUrl;

    public PairRecommendationShareResultResponse getSharedResult(String shareToken) {
        SharedPairRecommendation sharedPairRecommendation = findSharedPairRecommendation(shareToken);

        List<Long> cocktailIds = List.of(
                sharedPairRecommendation.getCocktailId1(),
                sharedPairRecommendation.getCocktailId2(),
                sharedPairRecommendation.getCocktailId3()
        );
        Map<Long, Cocktail> cocktailsById = cocktailRepository.findAllById(cocktailIds).stream()
                .collect(Collectors.toMap(Cocktail::getId, Function.identity()));

        List<Integer> matchScores = List.of(
                sharedPairRecommendation.getMatchScore1(),
                sharedPairRecommendation.getMatchScore2(),
                sharedPairRecommendation.getMatchScore3()
        );

        // TODO: 칵테일이 삭제되어 findAllById 결과에서 빠지는 경우에 대한 처리 정책은 팀 확인 필요
        List<SharedRecommendedCocktailResponse> recommendations = new ArrayList<>();
        for (int i = 0; i < cocktailIds.size(); i++) {
            Cocktail cocktail = cocktailsById.get(cocktailIds.get(i));
            if (cocktail == null) {
                continue;
            }
            recommendations.add(SharedRecommendedCocktailResponse.of(
                    cocktail,
                    i + 1,
                    matchScores.get(i)
            ));
        }

        TasteProfile compromiseProfile = TasteProfile.of(
                sharedPairRecommendation.getCompromiseAlcoholIntensity(),
                sharedPairRecommendation.getCompromiseSweetness(),
                sharedPairRecommendation.getCompromiseSourness(),
                sharedPairRecommendation.getCompromiseRefreshing(),
                sharedPairRecommendation.getCompromiseBitterness()
        );

        return PairRecommendationShareResultResponse.of(
                CompromiseProfileResponse.from(compromiseProfile),
                recommendations,
                sharedPairRecommendation.getMyMatchScore(),
                sharedPairRecommendation.getPartnerMatchScore(),
                sharedPairRecommendation.getThumbnailImageUrl()
        );
    }

    public PairRecommendationSharePageResponse getSharePage(String shareToken) {
        SharedPairRecommendation sharedPairRecommendation = findSharedPairRecommendation(shareToken);
        String sharePath = SHARE_PATH + shareToken;

        return new PairRecommendationSharePageResponse(
                normalizeBaseUrl(shareBaseUrl) + sharePath,
                normalizeBaseUrl(shareFrontendBaseUrl) + sharePath,
                sharedPairRecommendation.getThumbnailImageUrl()
        );
    }

    private SharedPairRecommendation findSharedPairRecommendation(String shareToken) {
        return sharedPairRecommendationRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new RestApiException(SHARE_TOKEN_NOT_FOUND));
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
