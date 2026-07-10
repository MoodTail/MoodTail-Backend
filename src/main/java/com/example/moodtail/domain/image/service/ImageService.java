package com.example.moodtail.domain.image.service;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.repository.ImageRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.ImageErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new RestApiException(ImageErrorStatus.IMAGE_NOT_FOUND));
    }

    public String getImageUrl(Long imageId) {
        if (imageId == null) {
            return null; // DB에 이미지 ID 자체가 없는 경우 (필요시 기본 이미지 URL 문자열로 변경 가능)
        }
        return getImage(imageId).getImageUrl();
    }
}
