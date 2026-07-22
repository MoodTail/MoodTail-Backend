package com.example.moodtail.domain.collection.controller;

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
@Tag(
        name = "Collections",
        description = "도감 조회 및 공유 API"
)
@RequestMapping("/api/v1/collections")
public class CollectionController {
    private final CollectionService collectionService;
    private final CollectionShareService collectionShareService;

    @GetMapping
    @Operation(summary = "도감 전체 조회")
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

    @Operation(summary = "도감 공유 이미지 저장",
            description = """
                  프론트엔드에서 생성한 도감 공유 이미지를 저장하고 공유 URL을 반환합니다.
                  사용자당 공유 데이터는 하나만 유지,
                  다시 공유 시 토큰은 유지하고 이미지와 버전 갱신.
                  """)
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

    @Operation(
            summary = "공유 도감 조회",
            description = """
                  공유 토큰을 사용하여 해당 사용자의 현재 도감 상태를 조회합니다.
                  공유 링크를 받은 사용자는 로그인하지 않아도 조회할 수 있습니다.
                  """
    )
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
