package com.example.moodtail.domain.inquiry.service;

import com.example.moodtail.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.moodtail.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.moodtail.domain.inquiry.entity.Inquiry;
import com.example.moodtail.domain.inquiry.entity.InquiryType;
import com.example.moodtail.domain.inquiry.repository.InquiryRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.INVALID_ROLE;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.InquiryErrorStatus.INVALID_INQUIRY;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_CONTACT_EMAIL_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public InquiryCreateResponse createInquiry(
            InquiryCreateRequest request,
            PrincipalDetails principalDetails
    ) {
        if (request == null) {
            throw new RestApiException(INVALID_INQUIRY);
        }
        InquiryType inquiryType = parseInquiryType(request.inquiryType());
        String content = validateContent(request.content());
        User user = findInquiryUser(principalDetails);
        String contactEmail = validateContactEmail(request.contactEmail(), user != null);

        Inquiry inquiry = Inquiry.create(user, contactEmail, inquiryType, content);
        return InquiryCreateResponse.from(inquiryRepository.save(inquiry));
    }

    private InquiryType parseInquiryType(String inquiryType) {
        if (inquiryType == null || inquiryType.isBlank()) {
            throw new RestApiException(INVALID_INQUIRY);
        }
        try {
            return InquiryType.valueOf(inquiryType);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(INVALID_INQUIRY);
        }
    }

    private String validateContent(String content) {
        if (content == null) {
            throw new RestApiException(INVALID_INQUIRY);
        }
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_CONTENT_LENGTH || trimmedContent.length() > MAX_CONTENT_LENGTH) {
            throw new RestApiException(INVALID_INQUIRY);
        }
        return trimmedContent;
    }

    private String validateContactEmail(String contactEmail, boolean isLoggedInUser) {
        if (contactEmail == null || contactEmail.isBlank()) {
            if (isLoggedInUser) {
                return null;
            }
            throw new RestApiException(INVALID_INQUIRY);
        }

        String trimmedContactEmail = contactEmail.trim();
        if (trimmedContactEmail.length() > MAX_CONTACT_EMAIL_LENGTH
                || !EMAIL_PATTERN.matcher(trimmedContactEmail).matches()) {
            throw new RestApiException(INVALID_INQUIRY);
        }
        return trimmedContactEmail;
    }

    private User findInquiryUser(PrincipalDetails principalDetails) {
        if (principalDetails == null || UserRole.GUEST.name().equals(principalDetails.getRole())) {
            return null;
        }
        if (!UserRole.USER.name().equals(principalDetails.getRole())
                && !UserRole.ADMIN.name().equals(principalDetails.getRole())) {
            throw new RestApiException(INVALID_ROLE);
        }
        return userRepository.findById(principalDetails.getUserId())
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
    }
}
