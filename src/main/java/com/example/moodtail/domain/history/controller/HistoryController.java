package com.example.moodtail.domain.history.controller;

import com.example.moodtail.domain.history.controller.docs.HistoryControllerDocs;
import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
import com.example.moodtail.domain.history.service.HistoryPhotoService;
import com.example.moodtail.domain.history.service.HistoryService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class HistoryController implements HistoryControllerDocs {

    private final HistoryService historyService;
    private final HistoryPhotoService historyPhotoService;

    @Override
    @GetMapping("/calendar")
    public BaseResponse<HistoryCalendarResponse> getHistoryCalendar(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return BaseResponse.onSuccess(historyService.getCalendar(principal.getUserId(), year, month));
    }

    @Override
    @GetMapping("/dates/{date}")
    public BaseResponse<HistoryDateResponse> getHistoryByDate(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable String date
    ) {
        return BaseResponse.onSuccess(historyService.getByDate(principal.getUserId(), date));
    }

    @Override
    @GetMapping("/test-results/{resultId}")
    public BaseResponse<HistoryTestResultDetailResponse> getTestResultDetail(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long resultId
    ) {
        return BaseResponse.onSuccess(historyService.getTestResultDetail(principal.getUserId(), resultId));
    }

    @Override
    @GetMapping("/drinking-records/{recordId}")
    public BaseResponse<HistoryDetailResponse> getHistoryDetail(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long recordId
    ) {
        return BaseResponse.onSuccess(historyService.getDetail(principal.getUserId(), recordId));
    }

    @Override
    @PostMapping("/drinking-records")
    public BaseResponse<HistoryCreateResponse> createHistory(
            @AuthenticationPrincipal PrincipalDetails principal,
            @Valid @RequestBody HistoryCreateRequest request
    ) {
        return BaseResponse.onSuccess(historyService.create(principal.getUserId(), request));
    }

    @Override
    @PatchMapping("/drinking-records/{recordId}")
    public BaseResponse<HistoryUpdateResponse> updateHistory(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long recordId,
            @Valid @RequestBody HistoryUpdateRequest request
    ) {
        return BaseResponse.onSuccess(historyService.update(principal.getUserId(), recordId, request));
    }

    @Override
    @DeleteMapping("/drinking-records/{recordId}")
    public BaseResponse<Void> deleteHistory(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable Long recordId
    ) {
        historyService.delete(principal.getUserId(), recordId);
        return BaseResponse.onSuccess(null);
    }

    @Override
    @PostMapping(
            path = "/dates/{date}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BaseResponse<HistoryPhotoResponse> addHistoryPhoto(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable String date,
            @RequestPart("image") MultipartFile image,
            @RequestParam("sourceType") String sourceType
    ) {
        return BaseResponse.onSuccess(
                historyPhotoService.add(principal.getUserId(), date, image, sourceType)
        );
    }

    @Override
    @DeleteMapping("/dates/{date}/photos/{photoId}")
    public BaseResponse<Void> deleteHistoryPhoto(
            @AuthenticationPrincipal PrincipalDetails principal,
            @PathVariable String date,
            @PathVariable Long photoId
    ) {
        historyPhotoService.delete(principal.getUserId(), date, photoId);
        return BaseResponse.onSuccess(null);
    }
}
