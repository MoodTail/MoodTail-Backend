package com.example.moodtail.domain.auth.controller;

import com.example.moodtail.domain.auth.service.AccountWithdrawalService;
import com.example.moodtail.domain.user.entity.UserRole;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountWithdrawalControllerTest {

    @Mock
    private AccountWithdrawalService accountWithdrawalService;

    @Test
    void memberCanWithdraw() {
        AccountWithdrawalController controller = new AccountWithdrawalController(accountWithdrawalService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.withdraw(new PrincipalDetails(9L, UserRole.USER), request, response);

        verify(accountWithdrawalService).withdraw(9L, request, response);
    }

    @Test
    void guestCannotUseMemberWithdrawal() {
        AccountWithdrawalController controller = new AccountWithdrawalController(accountWithdrawalService);

        assertThatThrownBy(() -> controller.withdraw(
                new PrincipalDetails(2L, UserRole.GUEST),
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                org.assertj.core.api.Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo("AUTH027")
        );

        verify(accountWithdrawalService, never()).withdraw(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
