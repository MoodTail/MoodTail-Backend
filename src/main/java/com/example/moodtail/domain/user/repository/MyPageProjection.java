package com.example.moodtail.domain.user.repository;

public interface MyPageProjection {

    Long getUserId();

    String getNickname();

    Long getMoodTypeId();

    String getMoodTypeCode();

    String getMoodTypeName();

    String getCharacterImageUrl();

    Long getTotalTestCount();

    Long getMonthlyRecordCount();

    Long getUnlockedMoodTypeCount();
}
