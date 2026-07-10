package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.dto.response.MyPageResponse.RepresentativeMoodTypeResponse;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.MyPageProjection;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.LOGIN_USER_REQUIRED;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;

    public MyPageResponse getMyPage(Long userId, String role) {
        validateRole(role);

        LocalDate monthStart = LocalDate.now(KOREA_ZONE_ID).withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        MyPageProjection projection = userRepository.findMyPageByUserId(
                        userId,
                        monthStart,
                        nextMonthStart
                )
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));

        RepresentativeMoodTypeResponse representativeMoodType = null;
        if (projection.getMoodTypeId() != null) {
            representativeMoodType = new RepresentativeMoodTypeResponse(
                    projection.getMoodTypeId(),
                    projection.getMoodTypeCode(),
                    projection.getMoodTypeName(),
                    projection.getCharacterImageUrl()
            );
        }

        return new MyPageResponse(
                projection.getUserId(),
                projection.getNickname(),
                representativeMoodType,
                projection.getTotalTestCount(),
                projection.getMonthlyRecordCount(),
                projection.getUnlockedMoodTypeCount()
        );
    }

    private void validateRole(String role) {
        if (UserRole.GUEST.name().equals(role)) {
            throw new RestApiException(LOGIN_USER_REQUIRED);
        }
        if (!UserRole.USER.name().equals(role) && !UserRole.ADMIN.name().equals(role)) {
            throw new RestApiException(INVALID_ROLE);
        }
    }
}
