package com.example.moodtail.global.auth.client;

import com.example.moodtail.global.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuthRestClientFactory {

    private final RestClient.Builder restClientBuilder;
    private final AuthProperties authProperties;

    public RestClient create() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(authProperties.oauth().connectTimeoutMillis()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(authProperties.oauth().readTimeoutMillis()));

        return restClientBuilder.clone()
                .requestFactory(requestFactory)
                .build();
    }
}
