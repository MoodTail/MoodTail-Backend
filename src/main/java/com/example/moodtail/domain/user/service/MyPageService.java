package com.example.moodtail.domain.user.service;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.user.dto.request.UserProfileUpdateRequest;
import com.example.moodtail.domain.user.dto.response.MyPageResponse;
import com.example.moodtail.domain.user.dto.response.MyPageResponse.RepresentativeMoodTypeResponse;
import com.example.moodtail.domain.user.dto.response.UserProfileUpdateResponse;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.MyPageProjection;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.domain.user.validator.NicknameValidator;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.LOGIN_USER_REQUIRED;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.INVALID_PROFILE_UPDATE;
import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

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

    @Transactional
    public UserProfileUpdateResponse updateProfile(
            Long userId,
            String role,
            UserProfileUpdateRequest request
    ) {
        validateRole(role);
        validateProfileUpdateRequest(request);
        String nickname = request.nickname() == null
                ? null
                : NicknameValidator.normalize(request.nickname());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        if (nickname != null) {
            user.updateNickname(nickname);
        }
        if (request.representativeMoodTypeId() != null) {
            UserUnlockedMoodType unlockedMoodType = userUnlockedMoodTypeRepository
                    .findByUserIdAndMoodTypeId(userId, request.representativeMoodTypeId())
                    .orElseThrow(() -> new RestApiException(REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED));
            user.updateRepresentativeMoodType(unlockedMoodType.getMoodType());
        }

        return UserProfileUpdateResponse.from(user);
    }

    private void validateProfileUpdateRequest(UserProfileUpdateRequest request) {
        if (request == null || (request.nickname() == null && request.representativeMoodTypeId() == null)) {
            throw new RestApiException(INVALID_PROFILE_UPDATE);
        }
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
