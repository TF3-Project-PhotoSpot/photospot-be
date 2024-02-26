package com.tf4.photospot.auth.infrastructure;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.global.config.jwt.JwtConstant;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JwtRedisRepository {
	private static final String REFRESH_TOKEN_PREFIX = "user:";
	private static final String REVOKED_ACCESS_TOKEN_VALUE = "revoked_access_token";

	private final RedisTemplate<String, String> redisTemplate;

	public void save(RefreshToken refreshToken) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		String key = REFRESH_TOKEN_PREFIX + refreshToken.getUserId();
		valueOperations.set(key, refreshToken.getToken());
		redisTemplate.expire(key, JwtConstant.REFRESH_TOKEN_DURATION.toMillis(), TimeUnit.MILLISECONDS);
	}

	public Optional<RefreshToken> findByUserId(Long userId) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		String refreshToken = valueOperations.get(REFRESH_TOKEN_PREFIX + userId);
		return Optional.ofNullable(refreshToken).map(token -> new RefreshToken(userId, token));
	}

	public void deleteByUserId(Long userId) {
		redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
	}

	public void saveAccessTokenInBlackList(String accessToken, Long expiration) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		valueOperations.set(accessToken, REVOKED_ACCESS_TOKEN_VALUE);
		redisTemplate.expire(accessToken, expiration, TimeUnit.MILLISECONDS);
	}

	public boolean existsBlacklist(String accessToken) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(accessToken));
	}
}
