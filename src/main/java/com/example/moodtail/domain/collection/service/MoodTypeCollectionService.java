package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.MoodTypesResponse;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.LOGIN_USER_REQUIRED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoodTypeCollectionService {

    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    public MoodTypesResponse getMoodTypes(Long userId, String role) {
        validateRole(role);
        return MoodTypesResponse.from(userUnlockedMoodTypeRepository.findAllMoodTypesByUserId(userId));
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
