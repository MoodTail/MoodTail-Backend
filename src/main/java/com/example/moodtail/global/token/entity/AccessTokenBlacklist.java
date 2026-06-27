package com.example.moodtail.global.token.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("access_token_blacklist")
public class AccessTokenBlacklist {

	@Id
	private String tokenId;
}
