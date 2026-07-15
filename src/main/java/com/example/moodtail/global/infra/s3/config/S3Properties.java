package com.example.moodtail.global.infra.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucket,
        String region,
        String accessKey,
        String secretKey
) {

    public S3Properties {
        if (!StringUtils.hasText(region)) {
            throw new IllegalArgumentException("AWS region must not be blank");
        }
        if (StringUtils.hasText(accessKey) != StringUtils.hasText(secretKey)) {
            throw new IllegalArgumentException("AWS access key and secret key must be configured together");
        }
    }

    public boolean hasStaticCredentials() {
        return StringUtils.hasText(accessKey);
    }
}
