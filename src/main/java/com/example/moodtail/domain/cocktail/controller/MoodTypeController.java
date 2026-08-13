package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.controller.docs.MoodTypeControllerDocs;
import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/mood-types")
public class MoodTypeController implements MoodTypeControllerDocs {

    private final CocktailService cocktailService;

    @Override
    @GetMapping("/{moodTypeId}")
    public BaseResponse<MoodTypeResponse> getMoodType(
            @PathVariable Long moodTypeId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        return BaseResponse.onSuccess(
                cocktailService.getMoodType(
                        moodTypeId,
                        principalDetails
                )
        );
    }
}