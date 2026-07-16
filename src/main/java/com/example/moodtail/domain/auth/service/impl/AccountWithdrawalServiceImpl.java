package com.example.moodtail.domain.auth.service.impl;

import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository;
import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository.WithdrawalResult;
import com.example.moodtail.domain.auth.service.AccountWithdrawalAssetCleanupService;
import com.example.moodtail.domain.auth.service.AccountWithdrawalService;
import com.example.moodtail.domain.auth.service.TokenSessionService;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountWithdrawalServiceImpl implements AccountWithdrawalService {

    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final AccountWithdrawalRepository accountWithdrawalRepository;
    private final TokenSessionService tokenSessionService;
    private final AccountWithdrawalAssetCleanupService assetCleanupService;

    @Override
    public void withdraw(Long userId, HttpServletRequest request, HttpServletResponse response) {
        tokenSessionService.logout(request, response);

        WithdrawalResult result = deleteAccountData(userId);
        tokenSessionService.clearWithdrawnUserState(userId);
        log.info(
                "Completed account withdrawal: userId={}, anonymizedTestResults={}, pendingAssets={}",
                userId,
                result.anonymizedTestResults(),
                result.pendingAssetDeletions().size()
        );
        assetCleanupService.cleanup(result.pendingAssetDeletions());
    }

    private WithdrawalResult deleteAccountData(Long userId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        WithdrawalResult result = template.execute(status -> {
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new RestApiException(AuthErrorStatus.USER_NOT_FOUND));
            if (user.getRole() != UserRole.USER || !user.isActive() || user.isDeleted()) {
                throw new RestApiException(AuthErrorStatus.LOGIN_USER_REQUIRED);
            }

            WithdrawalResult deleted = accountWithdrawalRepository.deleteAccountData(userId);
            if (deleted.deletedUsers() != 1) {
                throw new RestApiException(AuthErrorStatus.USER_NOT_FOUND);
            }
            return deleted;
        });
        if (result == null) {
            throw new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE);
        }
        return result;
    }
}
