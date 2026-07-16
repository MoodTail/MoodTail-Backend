package com.example.moodtail.domain.collection.controller;

import com.example.moodtail.domain.collection.dto.request.RepresentativeMoodTypeUpdateRequest;
import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.dto.response.RepresentativeMoodTypeUpdateResponse;
import com.example.moodtail.domain.collection.service.CollectionService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final CollectionService collectionService;

    @GetMapping
    public BaseResponse<CollectionResponse> getCollection(
            @AuthenticationPrincipal
            PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(
                collectionService.getCollection(
                        principalDetails.getUserId()
                )
        );
    }

    @PatchMapping("/representative-mood-type")
    @Operation(summary = "대표 무드 타입 변경")
    public BaseResponse<RepresentativeMoodTypeUpdateResponse>
    updateRepresentativeMoodType(
            @AuthenticationPrincipal
            PrincipalDetails principalDetails,

            @Valid
            @RequestBody
            RepresentativeMoodTypeUpdateRequest request
    ) {
        return BaseResponse.onSuccess(
                collectionService.updateRepresentativeMoodType(
                        principalDetails.getUserId(),
                        principalDetails.getRole(),
                        request.moodTypeId()
                )
        );
    }
}
