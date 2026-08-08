package com.example.moodtail.domain.weather.service;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.weather.client.KakaoLocalClient;
import com.example.moodtail.domain.weather.client.dto.KakaoRegionResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReverseGeocodingServiceTest {
    private static final double SEOUL_LATITUDE = 37.5665;
    private static final double SEOUL_LONGITUDE = 126.9780;

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @InjectMocks
    private ReverseGeocodingService reverseGeocodingService;

    @Test
    void resolvesRegionFromAdministrativeRegionResponse() {
        KakaoRegionResponse response = response(
                document("B", "서울특별시"),
                document("H", "서울특별시")
        );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        Region result = reverseGeocodingService.resolve(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        );

        assertThat(result).isEqualTo(Region.SEOUL);

        verify(kakaoLocalClient).getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        );
    }

    @Test
    void prefersAdministrativeRegionWhenBothTypesExist() {
        KakaoRegionResponse response = response(
                document("B", "서울특별시"),
                document("H", "부산광역시")
        );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        Region result = reverseGeocodingService.resolve(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        );

        assertThat(result).isEqualTo(Region.BUSAN);
    }

    @Test
    void usesFirstDocumentWhenAdministrativeRegionDoesNotExist() {
        KakaoRegionResponse response = response(
                document("B", "제주특별자치도")
        );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        Region result = reverseGeocodingService.resolve(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        );

        assertThat(result).isEqualTo(Region.JEJU);
    }

    @Test
    void rejectsEmptyDocumentsAsUnsupportedRegion() {
        KakaoRegionResponse response =
                new KakaoRegionResponse(
                        new KakaoRegionResponse.Meta(0),
                        List.of()
                );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        assertThatThrownBy(() ->
                reverseGeocodingService.resolve(
                        SEOUL_LATITUDE,
                        SEOUL_LONGITUDE
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION400")
        );
    }

    @Test
    void rejectsNullDocumentsAsInvalidProviderResponse() {
        KakaoRegionResponse response =
                new KakaoRegionResponse(
                        new KakaoRegionResponse.Meta(0),
                        null
                );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        assertThatThrownBy(() ->
                reverseGeocodingService.resolve(
                        SEOUL_LATITUDE,
                        SEOUL_LONGITUDE
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION502")
        );
    }

    @Test
    void rejectsUnsupportedKakaoRegionName() {
        KakaoRegionResponse response = response(
                document("H", "도쿄도")
        );

        given(kakaoLocalClient.getRegion(
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE
        )).willReturn(response);

        assertThatThrownBy(() ->
                reverseGeocodingService.resolve(
                        SEOUL_LATITUDE,
                        SEOUL_LONGITUDE
                )
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION400")
        );
    }

    private KakaoRegionResponse response(
            KakaoRegionResponse.Document... documents
    ) {
        return new KakaoRegionResponse(
                new KakaoRegionResponse.Meta(
                        documents.length
                ),
                List.of(documents)
        );
    }

    private KakaoRegionResponse.Document document(
            String regionType,
            String regionName
    ) {
        return new KakaoRegionResponse.Document(
                regionType,
                regionName
        );
    }
}
