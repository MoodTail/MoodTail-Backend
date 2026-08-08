package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.global.common.exception.RestApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import static com.example.moodtail.global.common.exception.code.status.RegionErrorStatus.REGION_NOT_SUPPORTED;

@Getter
@RequiredArgsConstructor
public enum Region {
    SEOUL("seoul", "서울특별시", 37.5665, 126.9780),
    BUSAN("busan", "부산광역시", 35.1796, 129.0756),
    DAEGU("daegu", "대구광역시", 35.8714, 128.6014),
    INCHEON("incheon", "인천광역시", 37.4563, 126.7052),
    GWANGJU("gwangju", "광주광역시", 35.1595, 126.8526),
    DAEJEON("daejeon", "대전광역시", 36.3504, 127.3845),
    ULSAN("ulsan", "울산광역시", 35.5395, 129.3114),
    SEJONG("sejong", "세종특별자치시", 36.4800, 127.2890),
    GYEONGGI("gyeonggi", "경기도", 37.2636, 127.0286),
    GANGWON("gangwon", "강원특별자치도", 37.8813, 127.7298),
    CHUNGBUK("chungbuk", "충청북도", 36.6424, 127.4890),
    CHUNGNAM("chungnam", "충청남도", 36.6012, 126.6608),
    JEONBUK("jeonbuk", "전북특별자치도", 35.8203, 127.1088),
    JEONNAM("jeonnam", "전라남도", 34.8161, 126.4629),
    GYEONGBUK("gyeongbuk", "경상북도", 36.5760, 128.5058),
    GYEONGNAM("gyeongnam", "경상남도", 35.2383, 128.6924),
    JEJU("jeju", "제주특별자치도", 33.4996, 126.5312);

    private final String key;

    private final String kakaoRegionName;

    private final double representativeLatitude;

    private final double representativeLongitude;

    public static Region fromKakaoRegionName(
            String regionName
    ) {
        if (regionName == null || regionName.isBlank()) {
            throw new RestApiException(REGION_NOT_SUPPORTED);
        }

        String normalizedName = normalize(regionName);

        return Arrays.stream(values())
                .filter(region ->
                        region.kakaoRegionName.equals(normalizedName)
                )
                .findFirst()
                .orElseThrow(() -> new RestApiException(REGION_NOT_SUPPORTED));
    }

    private static String normalize(String regionName) {
        String trimmedName = regionName.trim();

        // 명칭 변경 전 값이 반환 시 대응
        return switch (trimmedName) {
            case "강원도" -> "강원특별자치도";
            case "전라북도" -> "전북특별자치도";
            default -> trimmedName;
        };
    }
}
