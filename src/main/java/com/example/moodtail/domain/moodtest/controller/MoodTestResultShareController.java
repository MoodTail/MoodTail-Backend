package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.controller.docs.MoodTestResultShareControllerDocs;
import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultShareCreateRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultShareCreateResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestResultShareService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import com.example.moodtail.global.config.swagger.SwaggerBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tests/results/share")
public class MoodTestResultShareController implements MoodTestResultShareControllerDocs {

    private final MoodTestResultShareService moodTestResultShareService;

    @GetMapping("/{shareToken}")
    public BaseResponse<MoodTestResultResponse> getSharedResult(
            @PathVariable String shareToken
    ) {
        return BaseResponse.onSuccess(moodTestResultShareService.getSharedResult(shareToken));
    }

    // multipart의 request 파트를 Swagger에서 application/json으로 전송하기 위한 설정
    @SwaggerBody(content = @Content(
            encoding = @Encoding(
                    name = "request",
                    contentType = MediaType.APPLICATION_JSON_VALUE
            )
    ))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<MoodTestResultShareCreateResponse> createShare(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestPart("request") MoodTestResultShareCreateRequest request,
            @RequestPart("thumbnail") MultipartFile thumbnail
    ) {
        return BaseResponse.onSuccess(moodTestResultShareService.createShare(
                principalDetails.getUserId(),
                request,
                thumbnail
        ));
    }
}
