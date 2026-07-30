package com.example.moodtail.domain.collection.controller;

import com.example.moodtail.domain.collection.controller.docs.CollectionControllerDocs;
import com.example.moodtail.domain.collection.dto.request.RepresentativeMoodTypeUpdateRequest;
import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.dto.response.CollectionShareCreateResponse;
import com.example.moodtail.domain.collection.dto.response.RepresentativeMoodTypeUpdateResponse;
import com.example.moodtail.domain.collection.service.CollectionService;
import com.example.moodtail.domain.collection.service.CollectionShareService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collections")
public class CollectionController implements CollectionControllerDocs {
    private final CollectionService collectionService;
    private final CollectionShareService collectionShareService;

    @Override
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

    @Override
    @PatchMapping("/representative-mood-type")
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

    @Override
    @PostMapping(
            value = "/share",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BaseResponse<CollectionShareCreateResponse>
    createOrUpdateShare(
            @AuthenticationPrincipal
            PrincipalDetails principalDetails,

            @RequestPart("thumbnail")
            MultipartFile thumbnail
    ) {
        return BaseResponse.onSuccess(
                collectionShareService.createOrUpdateShare(
                        principalDetails.getUserId(),
                        thumbnail
                )
        );
    }

    @Override
    @GetMapping("/share/{shareToken}")
    public BaseResponse<CollectionResponse> getSharedCollection(
            @PathVariable String shareToken
    ) {
        return BaseResponse.onSuccess(
                collectionShareService.getSharedCollection(
                        shareToken
                )
        );
    }
}
