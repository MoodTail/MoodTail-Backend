package com.example.moodtail.domain.cocktail.controller;

import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
import com.example.moodtail.domain.cocktail.service.CocktailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/cocktails/")
public class CocktailController {

    private final CocktailService moodTypeService;

    @GetMapping("/{typeId}")
    @Operation(summary = "타입 정보 조회")
    public ResponseEntity<MoodTypeResponse> getMoodType(@PathVariable Long typeId){
        MoodTypeResponse response = moodTypeService.getMoodType(typeId);
        return ResponseEntity.ok(response);
    }

}
