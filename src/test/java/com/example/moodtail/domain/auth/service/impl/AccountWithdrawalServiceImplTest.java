package com.example.moodtail.domain.auth.service.impl;

import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository;
import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository.PendingAssetDeletion;
import com.example.moodtail.domain.auth.repository.AccountWithdrawalRepository.WithdrawalResult;
import com.example.moodtail.domain.auth.service.AccountWithdrawalAssetCleanupService;
import com.example.moodtail.domain.auth.service.TokenSessionService;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.AuthErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawalServiceImplTest {

    @Mock PlatformTransactionManager transactionManager;
    @Mock UserRepository userRepository;
    @Mock AccountWithdrawalRepository accountWithdrawalRepository;
    @Mock TokenSessionService tokenSessionService;
    @Mock AccountWithdrawalAssetCleanupService assetCleanupService;

    private AccountWithdrawalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountWithdrawalServiceImpl(
                transactionManager,
                userRepository,
                accountWithdrawalRepository,
                tokenSessionService,
                assetCleanupService
        );
        lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void withdrawalRevokesSessionBeforeDeletingDataAndCleansAssetsAfterCommit() {
        User user = member(9L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<PendingAssetDeletion> assets = List.of(new PendingAssetDeletion(31L, "https://example/a.jpg"));
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(user));
        when(accountWithdrawalRepository.deleteAccountData(9L))
                .thenReturn(new WithdrawalResult(1, 2, assets));

        service.withdraw(9L, request, response);

        InOrder order = inOrder(tokenSessionService, accountWithdrawalRepository, assetCleanupService);
        order.verify(tokenSessionService).logout(request, response);
        order.verify(accountWithdrawalRepository).deleteAccountData(9L);
        order.verify(tokenSessionService).clearWithdrawnUserState(9L);
        order.verify(assetCleanupService).cleanup(assets);
    }

    @Test
    void databaseDataIsNotDeletedWhenSessionRevocationFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new RestApiException(AuthErrorStatus.AUTH_INFRASTRUCTURE_UNAVAILABLE))
                .when(tokenSessionService).logout(request, response);

        assertThatThrownBy(() -> service.withdraw(9L, request, response))
                .isInstanceOf(RestApiException.class);

        verify(accountWithdrawalRepository, never()).deleteAccountData(any());
    }

    @Test
    void guestLoadedDuringWithdrawalCannotBeDeleted() {
        User guest = User.createGuest(
                "85cd8e17-aa7c-4f8f-912f-11db0e05fe93",
                "게스트",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(guest, "id", 9L);
        when(userRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(guest));

        assertThatThrownBy(() -> service.withdraw(
                9L,
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                org.assertj.core.api.Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH027")
        );

        verify(accountWithdrawalRepository, never()).deleteAccountData(any());
    }

    private User member(Long id) {
        User user = User.createMember("사용자", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
