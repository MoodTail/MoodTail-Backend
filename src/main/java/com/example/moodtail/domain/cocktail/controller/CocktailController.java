package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.dto.response.CocktailDetailResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteListResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailListResponse;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cocktails")
public class CocktailController {

    private final CocktailService cocktailService; // 칵테일 서비스로 변경

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

}
