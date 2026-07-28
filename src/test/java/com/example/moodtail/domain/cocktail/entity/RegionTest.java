package com.example.moodtail.domain.cocktail.entity;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegionTest {
    @Test
    void convertsKakaoRegionNameToRegion() {
        Region result =
                Region.fromKakaoRegionName("서울특별시");

        assertThat(result).isEqualTo(Region.SEOUL);
    }

    @Test
    void convertsPreviousGangwonNameToCurrentRegion() {
        Region result =
                Region.fromKakaoRegionName("강원도");

        assertThat(result).isEqualTo(Region.GANGWON);
    }

    @Test
    void convertsPreviousJeonbukNameToCurrentRegion() {
        Region result =
                Region.fromKakaoRegionName("전라북도");

        assertThat(result).isEqualTo(Region.JEONBUK);
    }

    @Test
    void trimsWhitespaceFromKakaoRegionName() {
        Region result =
                Region.fromKakaoRegionName("  부산광역시  ");

        assertThat(result).isEqualTo(Region.BUSAN);
    }

    @Test
    void rejectsNullRegionName() {
        assertThatThrownBy(() ->
                Region.fromKakaoRegionName(null)
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION400")
        );
    }

    @Test
    void rejectsBlankRegionName() {
        assertThatThrownBy(() ->
                Region.fromKakaoRegionName(" ")
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION400")
        );
    }

    @Test
    void rejectsUnsupportedRegionName() {
        assertThatThrownBy(() ->
                Region.fromKakaoRegionName("도쿄도")
        ).isInstanceOfSatisfying(
                RestApiException.class,
                exception -> assertThat(
                        exception.getErrorCode().getCode()
                ).isEqualTo("REGION400")
        );
    }
}
