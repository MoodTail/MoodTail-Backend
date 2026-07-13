package com.example.moodtail.domain.user.controller;

import com.example.moodtail.domain.collection.dto.response.MoodTypesResponse;
import com.example.moodtail.domain.collection.service.MoodTypeCollectionService;
import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.dto.response.UserProfileUpdateResponse;
import com.example.moodtail.domain.user.service.MyPageService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final MyPageService myPageService;
    private final MoodTypeCollectionService moodTypeCollectionService;

    @GetMapping("/me")
    public BaseResponse<MyPageResponse> getMyPage(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(myPageService.getMyPage(
                principalDetails.getUserId(),
                principalDetails.getRole()
        ));
    }

    @GetMapping("/me/mood-types")
    public BaseResponse<MoodTypesResponse> getMoodTypes(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(moodTypeCollectionService.getMoodTypes(
                principalDetails.getUserId(),
                principalDetails.getRole()
        ));
    }

    @PatchMapping("/me")
    public BaseResponse<UserProfileUpdateResponse> updateProfile(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody(required = false) UserProfileUpdateRequest request
    ) {
        return BaseResponse.onSuccess(myPageService.updateProfile(
                principalDetails.getUserId(),
                principalDetails.getRole(),
                request
        ));
    }
}
