package com.example.moodtail.domain.collection.controller;

import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.service.CollectionService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
