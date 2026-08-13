package com.example.moodtail.domain.history.controller.docs;

import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Concrete generic wrappers used only to make Springdoc response schemas deterministic. */
public final class HistoryApiResponseSchemas {

    private HistoryApiResponseSchemas() {
    }

    @Schema(name = "HistoryCalendarApiResponse")
    public static final class Calendar extends BaseResponse<HistoryCalendarResponse> {
        private Calendar() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryDateApiResponse")
    public static final class Date extends BaseResponse<HistoryDateResponse> {
        private Date() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryTestResultDetailApiResponse")
    public static final class TestResultDetail extends BaseResponse<HistoryTestResultDetailResponse> {
        private TestResultDetail() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryDrinkingRecordDetailApiResponse")
    public static final class DrinkingRecordDetail extends BaseResponse<HistoryDetailResponse> {
        private DrinkingRecordDetail() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryDrinkingRecordCreateApiResponse")
    public static final class DrinkingRecordCreate extends BaseResponse<List<HistoryCreateResponse>> {
        private DrinkingRecordCreate() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryDrinkingRecordUpdateApiResponse")
    public static final class DrinkingRecordUpdate extends BaseResponse<HistoryUpdateResponse> {
        private DrinkingRecordUpdate() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryPhotoApiResponse")
    public static final class Photo extends BaseResponse<HistoryPhotoResponse> {
        private Photo() {
            super(null, null, null);
        }
    }

    @Schema(name = "HistoryVoidApiResponse")
    public static final class VoidResponse extends BaseResponse<Void> {
        private VoidResponse() {
            super(null, null, null);
        }
    }
}
