package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.dto.response.RepresentativeMoodTypeUpdateResponse;
import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.CollectionProjection;
import com.example.moodtail.domain.collection.repository.CollectionRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.*;
import static com.example.moodtail.global.common.exception.code.status.UserErrorStatus.REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;

    public CollectionResponse getCollection(
            Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new RestApiException(USER_NOT_FOUND)
                );

        List<CollectionProjection> moodTypes =
                collectionRepository.findCollectionByUserId(userId);

        return CollectionResponse.of(
                user.getRepresentativeMoodType(),
                moodTypes
        );
    }

    @Transactional
    public RepresentativeMoodTypeUpdateResponse updateRepresentativeMoodType(
            Long userId,
            String role,
            Long moodTypeId
    ) {
        validateRole(role);

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new RestApiException(USER_NOT_FOUND)
                );

        UserUnlockedMoodType unlockedMoodType =
                userUnlockedMoodTypeRepository
                        .findByUserIdAndMoodTypeId(
                                userId,
                                moodTypeId
                        )
                        .orElseThrow(
                                () -> new RestApiException(
                                        REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED
                                )
                        );

        user.updateRepresentativeMoodType(
                unlockedMoodType.getMoodType()
        );

        return RepresentativeMoodTypeUpdateResponse.from(
                unlockedMoodType.getMoodType()
        );
    }

    private void validateRole(String role) {
        if (UserRole.GUEST.name().equals(role)) {
            throw new RestApiException(LOGIN_USER_REQUIRED);
        }

        if (!UserRole.USER.name().equals(role)
                && !UserRole.ADMIN.name().equals(role)) {
            throw new RestApiException(INVALID_ROLE);
        }
    }
}
