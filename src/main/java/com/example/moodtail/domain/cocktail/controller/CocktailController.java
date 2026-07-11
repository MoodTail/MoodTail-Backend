package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.dto.response.CocktailListResponse;
import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{typeId}")
    @Operation(summary = "타입 정보 조회")
    public BaseResponse<MoodTypeResponse> getMoodType(@PathVariable Long typeId) {
        return BaseResponse.onSuccess(cocktailService.getMoodType(typeId));
    }

}
