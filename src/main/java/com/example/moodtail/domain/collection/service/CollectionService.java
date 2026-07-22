package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.dto.response.RepresentativeMoodTypeUpdateResponse;
import com.example.moodtail.domain.collection.repository.CollectionProjection;
import com.example.moodtail.domain.collection.repository.CollectionRepository;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.*;
import static com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus.COCKTAIL_TYPE_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.CollectionErrorStatus.COLLECTION_REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final MoodTypeRepository moodTypeRepository;
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

        MoodType moodType =
                moodTypeRepository.findDetailById(moodTypeId)
                        .orElseThrow(() -> new RestApiException(COCKTAIL_TYPE_NOT_FOUND));

        boolean unlocked =
                userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(userId, moodTypeId);

        if (!unlocked) {
            throw new RestApiException(COLLECTION_REPRESENTATIVE_MOOD_TYPE_NOT_UNLOCKED);
        }

        user.updateRepresentativeMoodType(moodType);

        return RepresentativeMoodTypeUpdateResponse.from(moodType);
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
