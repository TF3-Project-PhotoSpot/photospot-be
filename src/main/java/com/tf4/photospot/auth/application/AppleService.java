package com.tf4.photospot.auth.application;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.request.AppleRevokeRequest;
import com.tf4.photospot.auth.application.request.GetAppleRefreshTokenRequest;
import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;
import com.tf4.photospot.auth.application.response.AuthUserInfoDto;
import com.tf4.photospot.auth.infrastructure.AppleClient;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppleService {

	private final AppleClient appleClient;

	@Value("${apple.client-id}")
	private String appleBundleId;

	// Todo : 애플에서 값 생성 후 application.yml 환경변수로 변경
	private String appleKeyId;

	private String appleTeamId;
	private String appleSignKeyFilePath;

	private static final String KID = "kid";
	private static final String ALG = "alg";
	private static final String APPLE_ID_SERVER = "https://appleid.apple.com";

	public AuthUserInfoDto getTokenInfo(String identifyToken, String nonce) {
		Claims claims = getAppleClaims(identifyToken);
		validateClaims(claims, nonce);
		return new AuthUserInfoDto(claims.getSubject());
	}

	public Claims getAppleClaims(String identifyToken) {
		try {
			Map<String, String> tokenHeaders = parseHeaders(identifyToken);
			PublicKey publicKey = generatePublicKey(getApplePublicKey(tokenHeaders));
			return Jwts.parserBuilder()
				.setSigningKey(publicKey)
				.build()
				.parseClaimsJws(identifyToken)
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
			throw new ApiException(AuthErrorCode.INVALID_APPLE_IDENTIFY_TOKEN);
		}
	}

	private ApplePublicKeyResponse.Key getApplePublicKey(Map<String, String> headers) {
		return appleClient.getPublicKey().getMatchedKeyBy(headers.get(KID), headers.get(ALG))
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_APPLE_IDENTIFY_TOKEN));
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

	private void validateClaims(Claims claims, String nonce) {
		validateValue(claims.get("nonce"), nonce);
		validateValue(claims.getIssuer(), APPLE_ID_SERVER);
		validateValue(claims.getAudience(), appleBundleId);
		validateExpiration(claims.getExpiration());
	}

	private void validateValue(Object claimValue, Object expectValue) {
		if (!expectValue.equals(claimValue)) {
			throw new ApiException(AuthErrorCode.INVALID_APPLE_IDENTIFY_TOKEN);
		}
	}

	private void validateExpiration(Date expiration) {
		if (expiration.before(new Date())) {
			throw new ApiException(AuthErrorCode.EXPIRED_APPLE_IDENTIFY_TOKEN);
		}
	}

	public void unlink(String authorizationCode) {
		String clientSecret = createClientSecret();
		String refreshToken = getRefreshToken(authorizationCode, clientSecret);
		AppleRevokeRequest request = AppleRevokeRequest.builder()
			.clientId(appleBundleId)
			.clientSecret(clientSecret)
			.token(refreshToken)
			.build();
		appleClient.revoke(request);
	}

	private String getRefreshToken(String authorizationCode, String clientSecret) {
		GetAppleRefreshTokenRequest request = GetAppleRefreshTokenRequest.builder()
			.clientId(appleBundleId)
			.clientSecret(clientSecret)
			.code(authorizationCode)
			.grantType("authorization_code").build();
		try {
			return appleClient.generateToken(request).getRefreshToken();
		} catch (Exception ex) {
			throw new ApiException(AuthErrorCode.INVALID_APPLE_AUTHORIZATION_CODE);
		}
	}

	private String createClientSecret() {
		Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
		return Jwts.builder()
			.setHeaderParam(KID, appleKeyId)
			.setHeaderParam(ALG, "ES256")
			.setIssuer(appleTeamId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(expirationDate)
			.setAudience(APPLE_ID_SERVER)
			.setSubject(appleBundleId)
			.signWith(getPrivateKey(), SignatureAlgorithm.ES256)
			.compact();
	}

	private PrivateKey getPrivateKey() {
		try {
			ClassPathResource resource = new ClassPathResource(appleSignKeyFilePath);
			String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
			PEMParser pemParser = new PEMParser(new StringReader(privateKey));
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			PrivateKeyInfo object = (PrivateKeyInfo)pemParser.readObject();
			return converter.getPrivateKey(object);
		} catch (IOException ex) {
			throw new ApiException(AuthErrorCode.INVALID_PRIVATE_KEY);
		}
	}
}
