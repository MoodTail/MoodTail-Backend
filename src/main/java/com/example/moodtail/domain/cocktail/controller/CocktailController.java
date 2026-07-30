package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.controller.docs.CocktailControllerDocs;
import com.example.moodtail.domain.cocktail.controller.docs.CocktailTrendControllerDocs;
import com.example.moodtail.domain.cocktail.dto.request.CustomCocktailRecommendationRequest;
import com.example.moodtail.domain.cocktail.dto.request.DailyCocktailRequest;
import com.example.moodtail.domain.cocktail.dto.response.*;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.domain.cocktail.service.CocktailTrendService;
import com.example.moodtail.domain.cocktail.service.DailyCocktailService;
import com.example.moodtail.domain.recommendation.service.CustomCocktailRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cocktails")
public class CocktailController implements CocktailControllerDocs, CocktailTrendControllerDocs {


    private final CocktailService cocktailService; // 칵테일 서비스로 변경
    private final DailyCocktailService dailyCocktailService;
    private final CocktailTrendService cocktailTrendService;

    private final CustomCocktailRecommendationService customCocktailRecommendationService;

    @Override
    @GetMapping
    public BaseResponse<CocktailListResponse> getCocktails(
            @RequestParam(required = false) BigDecimal minAlcoholDegree,
            @RequestParam(required = false) BigDecimal maxAlcoholDegree,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.getCocktails(
                minAlcoholDegree,
                maxAlcoholDegree,
                keyword,
                principalDetails
        ));
    }

    @Override
    @GetMapping("/{cocktailId}")
    public BaseResponse<CocktailDetailResponse> getCocktailDetail(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.getCocktailDetail(cocktailId, principalDetails));
    }

    @Override
    @GetMapping("/favorites")
    public BaseResponse<CocktailFavoriteListResponse> getFavoriteCocktails(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.getFavoriteCocktails(principalDetails));
    }

    @Override
    @PostMapping("/{cocktailId}/favorites")
    public BaseResponse<CocktailFavoriteResponse> addFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.addFavorite(cocktailId, principalDetails));
    }

    @Override
    @DeleteMapping("/{cocktailId}/favorites")
    public BaseResponse<CocktailFavoriteResponse> removeFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.removeFavorite(cocktailId, principalDetails));
    }

    @Override
    @PostMapping("/custom")
    public BaseResponse<CustomCocktailRecommendationResponse>
    recommendCustomCocktail(
            @Valid @RequestBody CustomCocktailRecommendationRequest request
    ) {
        return BaseResponse.onSuccess(
                customCocktailRecommendationService.recommend(request)
        );
    }

    @Override
    @GetMapping("/today")
    public BaseResponse<DailyCocktailResponse> getDailyCocktail(
            @Valid @ModelAttribute
            @ParameterObject
            DailyCocktailRequest request
            ) {
        return BaseResponse.onSuccess(
                dailyCocktailService.getOrCreateTodayCocktail(
                        request.latitude(),
                        request.longitude()
                )
        );
    }

    @Override
    @GetMapping("/trend")
    public BaseResponse<CocktailTrendResponse> getCocktailTrend() {
        return BaseResponse.onSuccess(cocktailTrendService.getTrend());
    }

}
