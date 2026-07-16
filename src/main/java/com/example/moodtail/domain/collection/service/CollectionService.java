package com.example.moodtail.domain.collection.service;

import com.example.moodtail.domain.collection.dto.response.CollectionResponse;
import com.example.moodtail.domain.collection.repository.CollectionProjection;
import com.example.moodtail.domain.collection.repository.CollectionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

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
}
