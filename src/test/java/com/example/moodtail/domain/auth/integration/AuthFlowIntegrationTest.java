package com.example.moodtail.domain.auth.integration;

import com.example.moodtail.global.auth.client.OAuthClient;
import com.example.moodtail.global.auth.model.SocialUserProfile;
import com.example.moodtail.global.auth.model.SocialProvider;
import com.example.moodtail.global.auth.mail.PasswordResetMailSender;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.show-sql=false",
                "logging.level.org.springframework.security=WARN",
                "jwt.secret=bW9vZHRhaWwtdGVzdC1zZWNyZXQta2V5LTMyYnl0ZXMhIQ==",
                "auth.oauth.google.enabled=true",
                "auth.oauth.google.client-id=integration-google-client",
                "auth.oauth.google.client-secret=integration-google-secret",
                "auth.oauth.google.redirect-uri=http://localhost:5173/auth/google/callback",
                "auth.oauth.kakao.enabled=false",
                "auth.local.password-reset.enabled=true",
                "auth.local.password-reset.sender=no-reply@example.com",
                "auth.local.password-reset.pepper=integration-password-reset-pepper-at-least-32-bytes"
        }
)
@EnabledIfEnvironmentVariable(named = "RUN_AUTH_INTEGRATION_TESTS", matches = "true")
class AuthFlowIntegrationTest {

    private static final String REDIS_KEY_PREFIX =
            "moodtail:integration:" + UUID.randomUUID() + ":auth:";

    private static final StubOAuthClient GOOGLE_STUB = new StubOAuthClient(
            SocialProvider.GOOGLE,
            new SocialUserProfile(
                    SocialProvider.GOOGLE,
                    "integration-google-user",
                    "integration@example.com",
                    "통합테스트사용자"
            )
    );
    private static final StubOAuthClient KAKAO_STUB = new StubOAuthClient(
            SocialProvider.KAKAO,
            new SocialUserProfile(SocialProvider.KAKAO, "integration-kakao-user", null, "카카오")
    );
    private static final StubPasswordResetMailSender PASSWORD_RESET_MAIL_STUB =
            new StubPasswordResetMailSender();

    @TestBean(name = "googleOAuthClient", methodName = "googleOAuthClient")
    OAuthClient googleOAuthClient;

    @TestBean(name = "kakaoOAuthClient", methodName = "kakaoOAuthClient")
    OAuthClient kakaoOAuthClient;

    @TestBean(name = "smtpPasswordResetMailSender", methodName = "passwordResetMailSender")
    PasswordResetMailSender passwordResetMailSender;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @MockitoSpyBean
    private RedisRepository redisRepository;

    private Long requiredTermId;

    static OAuthClient googleOAuthClient() {
        return GOOGLE_STUB;
    }

    static OAuthClient kakaoOAuthClient() {
        return KAKAO_STUB;
    }

    static PasswordResetMailSender passwordResetMailSender() {
        return PASSWORD_RESET_MAIL_STUB;
    }

    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        String databaseHost = requiredEnvironment("AUTH_TEST_DATABASE_HOST");
        String databaseName = requiredEnvironment("AUTH_TEST_DATABASE_NAME");
        if (!"DELETE_AUTH_TEST_DATA".equals(requiredEnvironment("AUTH_TEST_DATABASE_RESET_CONFIRMATION"))) {
            throw new IllegalStateException(
                    "AUTH_TEST_DATABASE_RESET_CONFIRMATION must be DELETE_AUTH_TEST_DATA"
            );
        }
        if (!databaseName.toLowerCase(java.util.Locale.ROOT).contains("test")) {
            throw new IllegalStateException("AUTH_TEST_DATABASE_NAME must identify a dedicated test database");
        }
        int redisDatabase = Integer.parseInt(requiredEnvironment("AUTH_TEST_REDIS_DATABASE"));
        if (redisDatabase <= 0) {
            throw new IllegalStateException("AUTH_TEST_REDIS_DATABASE must select a non-default dedicated Redis DB");
        }
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:mysql://" + databaseHost + ":3306/" + databaseName
                        + "?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
        );
        registry.add("spring.datasource.username", () -> requiredEnvironment("AUTH_TEST_DATABASE_USERNAME"));
        registry.add("spring.datasource.password", () -> requiredEnvironment("AUTH_TEST_DATABASE_PASSWORD"));
        registry.add("spring.data.redis.host", () -> requiredEnvironment("AUTH_TEST_REDIS_HOST"));
        registry.add(
                "spring.data.redis.port",
                () -> Integer.parseInt(requiredEnvironment("AUTH_TEST_REDIS_PORT"))
        );
        registry.add(
                "spring.data.redis.database",
                () -> redisDatabase
        );
        registry.add("auth.redis.key-prefix", () -> REDIS_KEY_PREFIX);
    }

    @BeforeEach
    void resetInfrastructure() {
        clearIntegrationRedisKeys();
        jdbcTemplate.update("delete from user_term_agreements");
        jdbcTemplate.update("delete from social_accounts");
        jdbcTemplate.update("delete from local_accounts");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from terms");
        jdbcTemplate.update(
                "insert into terms (term_type, title, content, is_required, version, is_active, created_at) "
                        + "values ('SERVICE', '서비스 이용약관', '통합 테스트 약관', true, 'integration-1', true, now())"
        );
        requiredTermId = jdbcTemplate.queryForObject(
                "select id from terms where version = 'integration-1'",
                Long.class
        );
        GOOGLE_STUB.reset();
        PASSWORD_RESET_MAIL_STUB.reset();
    }

    @AfterEach
    void cleanInfrastructure() {
        clearIntegrationRedisKeys();
        jdbcTemplate.update("delete from user_term_agreements");
        jdbcTemplate.update("delete from social_accounts");
        jdbcTemplate.update("delete from local_accounts");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from terms");
    }

    private void clearIntegrationRedisKeys() {
        List<byte[]> keys = new ArrayList<>();
        try (RedisConnection connection = redisConnectionFactory.getConnection();
             Cursor<byte[]> cursor = connection.keyCommands().scan(
                     ScanOptions.scanOptions().match(REDIS_KEY_PREFIX + "*").count(1_000).build()
             )) {
            cursor.forEachRemaining(keys::add);
            jdbcTemplate.queryForList("select id from users", Long.class).forEach(userId -> {
                keys.add(("usage:in:" + userId).getBytes(StandardCharsets.UTF_8));
                keys.add(("usage:trigger:" + userId).getBytes(StandardCharsets.UTF_8));
            });
            if (!keys.isEmpty()) {
                connection.keyCommands().del(keys.toArray(byte[][]::new));
            }
        }
    }

    @Test
    void guestToUnifiedSocialSignupAndLoginRunsThroughSecurityMysqlAndRedis() throws Exception {
        String firstGuestToken = guestLogin(UUID.randomUUID());
        JsonNode obsoleteState = issueState(firstGuestToken);
        JsonNode firstState = issueState(firstGuestToken);
        String state = firstState.path("state").asText();
        String codeChallenge = firstState.path("codeChallenge").asText();

        ResponseEntity<String> obsoleteStateResponse = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/login/google",
                Map.of(
                        "authorizationCode", "obsolete-google-code",
                        "state", obsoleteState.path("state").asText()
                ),
                null
        );
        assertThat(obsoleteStateResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(objectMapper.readTree(obsoleteStateResponse.getBody()).path("code").asText())
                .isEqualTo("AUTH018");

        ResponseEntity<String> signupResponse = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/login/google",
                Map.of(
                        "authorizationCode", "first-google-code",
                        "state", state,
                        "nickname", "가입완료사용자",
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                null
        );
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode signupResult = objectMapper.readTree(signupResponse.getBody()).path("result");
        assertThat(signupResult.path("isNewUser").asBoolean()).isTrue();
        assertThat(createChallenge(GOOGLE_STUB.lastCodeVerifier.get())).isEqualTo(codeChallenge);
        assertThat(signupResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("HttpOnly", "SameSite=Lax");
        String firstUserAccessToken = signupResult.path("accessToken").asText();

        String secondGuestToken = guestLogin(UUID.randomUUID());
        JsonNode secondState = issueState(secondGuestToken);
        JsonNode existingLogin = postJson(
                "/api/v1/auth/login/google",
                Map.of(
                        "authorizationCode", "second-google-code",
                        "state", secondState.path("state").asText()
                ),
                null
        ).path("result");
        String secondUserAccessToken = existingLogin.path("accessToken").asText();

        assertThat(existingLogin.path("isNewUser").asBoolean()).isFalse();
        assertThat(getCurrentUser(firstUserAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getCurrentUser(secondUserAccessToken).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'USER' and deleted_at is null",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'GUEST' and deleted_at is not null",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void unifiedSocialSignupCommitsDatabaseBeforeRefreshSessionStorageAndCanRecoverByLogin() throws Exception {
        String guestToken = guestLogin(UUID.randomUUID());
        JsonNode state = issueState(guestToken);
        doThrow(new RedisConnectionFailureException("forced integration failure"))
                .doCallRealMethod()
                .when(redisRepository).saveRefreshJti(anyLong(), anyString());

        ResponseEntity<String> signupResponse = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/login/google",
                Map.of(
                        "authorizationCode", "failure-google-code",
                        "state", state.path("state").asText(),
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                null
        );

        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(objectMapper.readTree(signupResponse.getBody()).path("code").asText()).isEqualTo("AUTH028");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'USER' and deleted_at is null",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from social_accounts", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_term_agreements", Integer.class)).isEqualTo(1);

        String retryGuestToken = guestLogin(UUID.randomUUID());
        JsonNode retryState = issueState(retryGuestToken);
        JsonNode recoveredLogin = postJson(
                "/api/v1/auth/login/google",
                Map.of(
                        "authorizationCode", "retry-google-code",
                        "state", retryState.path("state").asText()
                ),
                null
        ).path("result");

        assertThat(recoveredLogin.path("isNewUser").asBoolean()).isFalse();
        assertThat(getCurrentUser(recoveredLogin.path("accessToken").asText()).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void localSignupAndLoginReuseTheSameGuestMergeAndTokenFlow() throws Exception {
        String signupGuestToken = guestLogin(UUID.randomUUID());
        JsonNode signup = postJson(
                "/api/v1/auth/signup/local",
                Map.of(
                        "email", "local-integration@example.com",
                        "password", "integration-password1",
                        "passwordConfirm", "integration-password1",
                        "nickname", "로컬통합사용자",
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                signupGuestToken
        ).path("result");
        assertThat(signup.path("isNewUser").asBoolean()).isTrue();
        String signupAccessToken = signup.path("accessToken").asText();

        String loginGuestToken = guestLogin(UUID.randomUUID());
        JsonNode login = postJson(
                "/api/v1/auth/login/local",
                Map.of(
                        "email", "LOCAL-INTEGRATION@example.com",
                        "password", "integration-password1"
                ),
                loginGuestToken
        ).path("result");

        assertThat(login.path("userId").asLong()).isEqualTo(signup.path("userId").asLong());
        assertThat(login.path("isNewUser").asBoolean()).isFalse();
        assertThat(getCurrentUser(signupAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getCurrentUser(login.path("accessToken").asText()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'USER' and deleted_at is null",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'GUEST' and deleted_at is not null",
                Integer.class
        )).isEqualTo(1);
    }

    @Test
    void refreshRotationAndLogoutInvalidateThePresentedSession() throws Exception {
        ResponseEntity<String> signupResponse = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/signup/local",
                Map.of(
                        "email", "session-integration@example.com",
                        "password", "integration-password1",
                        "passwordConfirm", "integration-password1",
                        "nickname", "세션통합사용자",
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                null
        );
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String originalAccessToken = objectMapper.readTree(signupResponse.getBody())
                .path("result").path("accessToken").asText();
        String originalRefreshCookie = cookiePair(signupResponse);

        ResponseEntity<String> reissueResponse = exchangeCookieAuthenticated(
                HttpMethod.POST,
                "/api/v1/auth/reissue",
                null,
                null,
                originalRefreshCookie
        );
        assertThat(reissueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String rotatedAccessToken = objectMapper.readTree(reissueResponse.getBody())
                .path("result").path("accessToken").asText();
        String rotatedRefreshCookie = cookiePair(reissueResponse);
        assertThat(getCurrentUser(originalAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getCurrentUser(rotatedAccessToken).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> logoutResponse = exchangeCookieAuthenticated(
                HttpMethod.POST,
                "/api/v1/auth/logout",
                null,
                rotatedAccessToken,
                rotatedRefreshCookie
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("Max-Age=0", "HttpOnly");
        assertThat(getCurrentUser(rotatedAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void passwordResetChangesCredentialAndRevokesExistingSession() throws Exception {
        JsonNode signup = postJson(
                "/api/v1/auth/signup/local",
                Map.of(
                        "email", "password-reset-integration@example.com",
                        "password", "old-integration-password1",
                        "passwordConfirm", "old-integration-password1",
                        "nickname", "비밀번호통합사용자",
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                null
        ).path("result");
        String oldAccessToken = signup.path("accessToken").asText();

        postJson(
                "/api/v1/auth/password-reset/codes",
                Map.of("email", "password-reset-integration@example.com"),
                null
        );
        String verificationCode = PASSWORD_RESET_MAIL_STUB.lastCode.get();
        assertThat(verificationCode).matches("[0-9]{6}");

        String resetToken = postJson(
                "/api/v1/auth/password-reset/codes/verify",
                Map.of(
                        "email", "password-reset-integration@example.com",
                        "code", verificationCode
                ),
                null
        ).path("result").path("resetToken").asText();
        assertThat(resetToken).isNotBlank();

        ResponseEntity<String> changeResponse = exchangeJson(
                HttpMethod.PATCH,
                "/api/v1/auth/password",
                Map.of(
                        "resetToken", resetToken,
                        "newPassword", "new-integration-password1",
                        "newPasswordConfirm", "new-integration-password1"
                ),
                null
        );
        assertThat(changeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getCurrentUser(oldAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> oldPasswordLogin = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/login/local",
                Map.of(
                        "email", "password-reset-integration@example.com",
                        "password", "old-integration-password1"
                ),
                null
        );
        assertThat(oldPasswordLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> newPasswordLogin = exchangeJson(
                HttpMethod.POST,
                "/api/v1/auth/login/local",
                Map.of(
                        "email", "password-reset-integration@example.com",
                        "password", "new-integration-password1"
                ),
                null
        );
        assertThat(newPasswordLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String guestLogin(UUID guestUuid) throws Exception {
        JsonNode result = postJson(
                "/api/v1/auth/guest",
                Map.of("guestUuid", guestUuid.toString()),
                null
        ).path("result");
        return result.path("accessToken").asText();
    }

    private JsonNode issueState(String guestAccessToken) throws Exception {
        return postJson(
                "/api/v1/auth/oauth-states/google",
                Map.of(),
                guestAccessToken
        ).path("result");
    }

    private ResponseEntity<String> getCurrentUser(String accessToken) {
        return exchangeJson(HttpMethod.GET, "/api/v1/users/me", null, accessToken);
    }

    private ResponseEntity<String> exchangeCookieAuthenticated(
            HttpMethod method,
            String path,
            Object body,
            String accessToken,
            String cookie
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ORIGIN, "http://localhost:" + port);
        headers.set(HttpHeaders.COOKIE, cookie);
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.exchange(
                "http://localhost:" + port + path,
                method,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    private String cookiePair(ResponseEntity<String> response) {
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotBlank();
        return setCookie.substring(0, setCookie.indexOf(';'));
    }

    private JsonNode postJson(String path, Object body, String accessToken) throws Exception {
        ResponseEntity<String> response = exchangeJson(HttpMethod.POST, path, body, accessToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody());
    }

    private ResponseEntity<String> exchangeJson(
            HttpMethod method,
            String path,
            Object body,
            String accessToken
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.exchange(
                "http://localhost:" + port + path,
                method,
                new HttpEntity<>(body, headers),
                String.class
        );
    }

    private static String createChallenge(String verifier) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required for the auth integration test");
        }
        return value;
    }

    private static final class StubOAuthClient implements OAuthClient {

        private final SocialProvider provider;
        private final SocialUserProfile profile;
        private final AtomicReference<String> lastCodeVerifier = new AtomicReference<>();

        private StubOAuthClient(SocialProvider provider, SocialUserProfile profile) {
            this.provider = provider;
            this.profile = profile;
        }

        @Override
        public SocialProvider provider() {
            return provider;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public SocialUserProfile requestUserProfile(
                String authorizationCode,
                String redirectUri,
                String codeVerifier
        ) {
            lastCodeVerifier.set(codeVerifier);
            return profile;
        }

        private void reset() {
            lastCodeVerifier.set(null);
        }
    }

    private static final class StubPasswordResetMailSender implements PasswordResetMailSender {

        private final AtomicReference<String> lastRecipient = new AtomicReference<>();
        private final AtomicReference<String> lastCode = new AtomicReference<>();

        @Override
        public void sendCode(String recipient, String code) {
            lastRecipient.set(recipient);
            lastCode.set(code);
        }

        private void reset() {
            lastRecipient.set(null);
            lastCode.set(null);
        }
    }
}
