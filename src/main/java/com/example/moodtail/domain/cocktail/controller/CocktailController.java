package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.dto.request.CustomCocktailRecommendationRequest;
import com.example.moodtail.domain.cocktail.dto.response.*;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.domain.cocktail.service.DailyCocktailService;
import com.example.moodtail.domain.recommendation.service.CustomCocktailRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cocktails")
public class CocktailController {

    private final CocktailService cocktailService; // 칵테일 서비스로 변경
    private final DailyCocktailService dailyCocktailService;

    private final CustomCocktailRecommendationService customCocktailRecommendationService;

    @GetMapping
    @Operation(
            summary = "칵테일 목록 조회",
            description = "도수 범위와 한글 또는 영문 이름으로 칵테일을 조회합니다."
    )
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

    @GetMapping("/{cocktailId}")
    @Operation(
            summary = "칵테일 정보/레시피 상세조회",
            description = "칵테일 정보, 레시피, 재료, 즐겨찾기 여부를 반환합니다. 인증이 필요하지 않습니다."
    )
    public BaseResponse<CocktailDetailResponse> getCocktailDetail(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.getCocktailDetail(cocktailId, principalDetails));
    }

    @GetMapping("/favorites")
    @Operation(
            summary = "칵테일 즐겨찾기 목록 조회",
            description = "인증된 사용자가 즐겨찾기한 칵테일 목록을 반환합니다."
    )
    public BaseResponse<CocktailFavoriteListResponse> getFavoriteCocktails(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.getFavoriteCocktails(principalDetails));
    }

    @PostMapping("/{cocktailId}/favorites")
    @Operation(
            summary = "칵테일 즐겨찾기 추가",
            description = "인증된 사용자가 칵테일을 즐겨찾기에 추가합니다."
    )
    public BaseResponse<CocktailFavoriteResponse> addFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.addFavorite(cocktailId, principalDetails));
    }

    @DeleteMapping("/{cocktailId}/favorites")
    @Operation(
            summary = "칵테일 즐겨찾기 삭제",
            description = "인증된 사용자가 칵테일을 즐겨찾기에서 삭제합니다."
    )
    public BaseResponse<CocktailFavoriteResponse> removeFavorite(
            @PathVariable Long cocktailId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(cocktailService.removeFavorite(cocktailId, principalDetails));
    }

    @PostMapping("/custom")
    @Operation(
            summary = "커스텀 칵테일 추천",
            description = "사용자가 설정한 5가지 맛 지표와 유클리드 거리가 가장 가까운 칵테일 하나를 추천합니다. "
    )
    public BaseResponse<CustomCocktailRecommendationResponse>
    recommendCustomCocktail(
            @Valid @RequestBody CustomCocktailRecommendationRequest request
    ) {
        return BaseResponse.onSuccess(
                customCocktailRecommendationService.recommend(request)
        );
    }


    @GetMapping("/today")
    @Operation(
            summary = "오늘의 칵테일 조회",
            description = "날씨를 기준으로 오늘의 칵테일을 추천합니다.\n" +
                    "                      오늘 추천이 이미 생성된 경우 저장된 추천을 반환합니다."
    )
    public BaseResponse<DailyCocktailResponse> getDailyCocktail() {
        return BaseResponse.onSuccess(
                dailyCocktailService.getOrCreateTodayCocktail()
        );
    }

}
