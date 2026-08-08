package com.example.moodtail.domain.weather.service;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.weather.client.KakaoLocalClient;
import com.example.moodtail.domain.weather.client.dto.KakaoRegionResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RegionErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {
    // 카카오 응답 행정동/법정동 응답 중 행정동 데이터 우선 선택
    private static final String ADMINISTRATIVE_REGION_TYPE = "H";

    private final KakaoLocalClient kakaoLocalClient;

    public Region resolve(
            double latitude,
            double longitude
    ) {
        KakaoRegionResponse response =
                kakaoLocalClient.getRegion(
                        latitude,
                        longitude
                );

        List<KakaoRegionResponse.Document> documents =
                response.documents();

        if (documents == null) {
            throw new RestApiException(
                    RegionErrorStatus.INVALID_REGION_RESPONSE
            );
        }

        if (documents.isEmpty()) {
            // 바다 또는 카카오가 지원하지 않는 좌표
            throw new RestApiException(
                    RegionErrorStatus.REGION_NOT_SUPPORTED
            );
        }

        KakaoRegionResponse.Document document =
                findAdministrativeRegion(documents);

        return Region.fromKakaoRegionName(
                document.region1DepthName()
        );
    }

    private KakaoRegionResponse.Document findAdministrativeRegion(
            List<KakaoRegionResponse.Document> documents
    ) {
        return documents.stream()
                .filter(document ->
                        ADMINISTRATIVE_REGION_TYPE.equals(
                                document.regionType()
                        )
                )
                .findFirst()
                .orElseGet(() -> documents.get(0));
    }
}
