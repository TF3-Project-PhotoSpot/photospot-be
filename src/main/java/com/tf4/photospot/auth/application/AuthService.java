package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.tf4.photospot.auth.presentation.request.KakaoUnlinkCallbackInfo;
import com.tf4.photospot.auth.util.NicknameGenerator;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.request.LoginUserInfo;
import com.tf4.photospot.user.application.response.OauthLoginResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.infrastructure.UserQueryRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserRepository userRepository;
	private final UserQueryRepository userQueryRepository;
	private final JwtRedisRepository jwtRedisRepository;
	private final JwtService jwtService;
	private final AppleService appleService;
	private final KakaoService kakaoService;

	private static final int NICKNAME_GENERATOR_RETRY_MAX = 5;

	@Transactional
	public OauthLoginResponse kakaoLogin(String accessToken, String id) {
		return oauthLogin(OauthAttributes.KAKAO.getProvider(), validateAndGetKakaoUserInfo(accessToken, id));
	}

	@Transactional
	public OauthLoginResponse appleLogin(String identifyToken, String nonce) {
		return oauthLogin(OauthAttributes.APPLE.getProvider(), validateAndGetAppleUserInfo(identifyToken, nonce));
	}

	public OauthLoginResponse oauthLogin(String provider, String account) {
		return userRepository.findUserByProviderTypeAndAccount(provider, account)
			.map(findUser -> OauthLoginResponse.from(true, findUser))
			.orElseGet(() -> OauthLoginResponse.from(false,
				userRepository.save(new LoginUserInfo(provider, account).toUser(generateNickname()))));
	}

	private String validateAndGetKakaoUserInfo(String accessToken, String id) {
		return kakaoService.getTokenInfo(accessToken, id).account();
	}

	private String validateAndGetAppleUserInfo(String identifyToken, String nonce) {
		return appleService.getTokenInfo(identifyToken, nonce).account();
	}

	private String generateNickname() {
		for (int attempt = 0; attempt < NICKNAME_GENERATOR_RETRY_MAX; attempt++) {
			String nickname = NicknameGenerator.generateRandomNickname();
			if (!isNicknameDuplicated(nickname)) {
				return nickname;
			}
		}
		throw new ApiException(AuthErrorCode.UNEXPECTED_NICKNAME_GENERATE_FAIL);
	}

	private boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	public ReissueTokenResponse reissueToken(Long userId, String refreshToken) {
		jwtService.validateRefreshToken(userId, refreshToken);
		User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		return new ReissueTokenResponse(jwtService.issueAccessToken(user.getId(), user.getRole().getType()),
			jwtService.issueRefreshToken(user.getId()));
	}

	@Transactional
	public void logout(String accessToken) {
		Claims claims = jwtService.parseAccessToken(accessToken);
		User user = userRepository.findById(claims.get(JwtConstant.USER_ID, Long.class))
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		jwtRedisRepository.saveAccessTokenInBlackList(accessToken, claims.getExpiration().getTime());
		jwtRedisRepository.deleteByUserId(user.getId());
	}

	public void existsBlacklist(String accessToken) {
		jwtService.validateAccessToken(accessToken);
		if (jwtRedisRepository.existsBlacklist(accessToken)) {
			throw new ApiException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}
	}

	public void unlinkKakaoAccount(String account) {
		kakaoService.unlink(Long.valueOf(account));
	}

	// Todo : 액세스토큰, 리프레시토큰 처리 추가
	@Transactional
	public void deleteUser(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(AuthErrorCode.NOT_FOUND_USER));
		userQueryRepository.deleteByUserId(user.getId());
	}

	// Todo : db 업데이트 실패했을 때 재시도하는 로직 추가?
	@Transactional
	public void deleteUnlinkedKakaoUser(String adminKey, KakaoUnlinkCallbackInfo info) {
		kakaoService.validateRequest(adminKey, info.appId());
		userRepository.findUserByProviderTypeAndAccount(OauthAttributes.KAKAO.getProvider(), info.account())
			.ifPresentOrElse(user -> {
				try {
					userQueryRepository.deleteByUserId(user.getId());
					createCallbackLog("성공", info, null);
				} catch (Exception ex) {
					createCallbackLog("실패", info, "DB 업데이트 과정에서 오류가 발생했습니다.");
				}
			}, () -> createCallbackLog("실패", info, "존재하지 않거나 이미 탈퇴한 사용자입니다."));
	}

	private void createCallbackLog(String result, KakaoUnlinkCallbackInfo info, String reason) {
		String baseMsg = String.format("카카오 연결 끊기 콜백 처리 %s, 사용자 계정 : %s, 요청 경로 : %s", result, info.account(),
			info.refererType());
		String fullMsg = reason != null ? baseMsg + ", 비고 : " + reason : baseMsg;
		log.info(fullMsg);
	}
}
