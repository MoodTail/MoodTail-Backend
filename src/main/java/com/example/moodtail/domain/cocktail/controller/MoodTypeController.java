package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/mood-types")
public class MoodTypeController {

    private final CocktailService cocktailService;

    @GetMapping("/{typeId}")
    @Operation(summary = "타입 정보 조회")
    public BaseResponse<MoodTypeResponse> getMoodType(@PathVariable Long typeId) {
        return BaseResponse.onSuccess(cocktailService.getMoodType(typeId));
    }
}