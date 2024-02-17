package com.tf4.photospot.auth.application;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;
import com.tf4.photospot.auth.application.response.AuthUserInfoDto;
import com.tf4.photospot.auth.infrastructure.AppleClient;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppleService {

	private final AppleClient appleClient;

	public AuthUserInfoDto getTokenInfo(String identityToken, String nonce) {
		Claims claims = getAppleClaims(identityToken);
		validateClaims(claims, nonce);
		return new AuthUserInfoDto(claims.getSubject());
	}

	public Claims getAppleClaims(String identityToken) {
		try {
			Map<String, String> tokenHeaders = parseHeaders(identityToken);
			PublicKey publicKey = generatePublicKey(getApplePublicKey(tokenHeaders));
			return Jwts.parserBuilder()
				.setSigningKey(publicKey)
				.build()
				.parseClaimsJws(identityToken)
				.getBody();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new ApiException(AuthErrorCode.CRYPTO_KEY_PROCESSING_ERROR);
		}
	}

	public Map<String, String> parseHeaders(String token) {
		try {
			return new ObjectMapper().readValue(JWT.decode(token).getHeader(), new TypeReference<>() {
			});
		} catch (JsonProcessingException | ArrayIndexOutOfBoundsException ex) {
			throw new ApiException(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN);
		}
	}

	private ApplePublicKeyResponse.Key getApplePublicKey(Map<String, String> headers) {
		return appleClient.getPublicKey().getMatchedKeyBy(headers.get("kid"), headers.get("alg"))
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN));
	}

	private PublicKey generatePublicKey(ApplePublicKeyResponse.Key key) throws
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		byte[] nBytes = Base64.getUrlDecoder().decode(key.getModulus());
		byte[] eBytes = Base64.getUrlDecoder().decode(key.getExponent());

		BigInteger intN = new BigInteger(1, nBytes);
		BigInteger intE = new BigInteger(1, eBytes);

		return KeyFactory.getInstance(key.getKty()).generatePublic(new RSAPublicKeySpec(intN, intE));
	}

	// Todo : client_id 설정, 문자열 관리
	private void validateClaims(Claims claims, String nonce) {
		validateValue(claims.get("nonce"), nonce);
		validateValue(claims.getIssuer(), "https://appleid.apple.com");
		validateValue(claims.getAudience(), "client_id");
		validateExpiration(claims.getExpiration());
	}

	private void validateValue(Object claimValue, Object expectValue) {
		if (!expectValue.equals(claimValue)) {
			throw new ApiException(AuthErrorCode.INVALID_APPLE_IDENTITY_TOKEN);
		}
	}

	private void validateExpiration(Date expiration) {
		if (expiration.before(new Date())) {
			throw new ApiException(AuthErrorCode.EXPIRED_APPLE_IDENTITY_TOKEN);
		}
	}
}
