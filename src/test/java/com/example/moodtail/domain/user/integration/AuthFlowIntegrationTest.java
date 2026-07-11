package com.example.moodtail.domain.user.integration;

import com.example.moodtail.domain.user.client.OAuthClient;
import com.example.moodtail.domain.user.client.SocialUserProfile;
import com.example.moodtail.domain.user.enums.SocialProvider;
import com.example.moodtail.global.token.repository.redis.RedisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
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
                "auth.redis.key-prefix=moodtail:integration:auth:"
        }
)
@EnabledIfEnvironmentVariable(named = "RUN_AUTH_INTEGRATION_TESTS", matches = "true")
class AuthFlowIntegrationTest {

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

    @TestBean(name = "googleOAuthClient", methodName = "googleOAuthClient")
    OAuthClient googleOAuthClient;

    @TestBean(name = "kakaoOAuthClient", methodName = "kakaoOAuthClient")
    OAuthClient kakaoOAuthClient;

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

    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        String databaseHost = requiredEnvironment("AUTH_TEST_DATABASE_HOST");
        String databaseName = requiredEnvironment("AUTH_TEST_DATABASE_NAME");
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
    }

    @BeforeEach
    void resetInfrastructure() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
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
        assertThat(getTerms(firstUserAccessToken).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getTerms(secondUserAccessToken).getStatusCode()).isEqualTo(HttpStatus.OK);
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
    void unifiedSocialSignupRollsBackDatabaseWhenRefreshSessionStorageFails() throws Exception {
        String guestToken = guestLogin(UUID.randomUUID());
        JsonNode state = issueState(guestToken);
        doThrow(new RedisConnectionFailureException("forced integration failure"))
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
                "select count(*) from users where role = 'GUEST' and deleted_at is null",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from social_accounts", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_term_agreements", Integer.class)).isZero();
    }

    @Test
    void localSignupAndLoginReuseTheSameGuestMergeAndTokenFlow() throws Exception {
        String signupGuestToken = guestLogin(UUID.randomUUID());
        JsonNode signup = postJson(
                "/api/v1/auth/signup/local",
                Map.of(
                        "email", "local-integration@example.com",
                        "password", "integration-password",
                        "passwordConfirm", "integration-password",
                        "nickname", "로컬통합사용자",
                        "agreements", new Object[]{Map.of("termId", requiredTermId, "agreed", true)}
                ),
                signupGuestToken
        ).path("result");
        assertThat(signup.path("isNewUser").asBoolean()).isTrue();

        String loginGuestToken = guestLogin(UUID.randomUUID());
        JsonNode login = postJson(
                "/api/v1/auth/login/local",
                Map.of(
                        "email", "LOCAL-INTEGRATION@example.com",
                        "password", "integration-password"
                ),
                loginGuestToken
        ).path("result");

        assertThat(login.path("userId").asLong()).isEqualTo(signup.path("userId").asLong());
        assertThat(login.path("isNewUser").asBoolean()).isFalse();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'USER' and deleted_at is null",
                Integer.class
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where role = 'GUEST' and deleted_at is not null",
                Integer.class
        )).isEqualTo(1);
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

    private ResponseEntity<String> getTerms(String accessToken) {
        return exchangeJson(HttpMethod.GET, "/api/v1/terms", null, accessToken);
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
}
