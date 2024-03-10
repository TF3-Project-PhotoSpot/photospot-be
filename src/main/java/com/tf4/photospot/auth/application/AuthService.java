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
import com.tf4.photospot.global.util.SlackAlert;
import com.tf4.photospot.user.application.UserService;
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
	private final UserService userService;
	private final SlackAlert slackAlert;

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
		User user = userService.getActiveUser(userId);
		return new ReissueTokenResponse(jwtService.issueAccessToken(user.getId(), user.getRole().getType()),
			jwtService.issueRefreshToken(user.getId()));
	}

	@Transactional
	public void logout(String accessToken) {
		Claims claims = jwtService.parseAccessToken(accessToken);
		User user = userService.getActiveUser(claims.get(JwtConstant.USER_ID, Long.class));
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

	@Transactional
	public void deleteUser(Long userId, String accessToken) {
		User user = userService.getActiveUser(userId);
		logout(accessToken);
		userQueryRepository.deleteByUserId(user.getId());
	}

	@Transactional
	public void deleteUnlinkedKakaoUser(String adminKey, KakaoUnlinkCallbackInfo info) {
		kakaoService.validateRequest(adminKey, info.appId());
		userRepository.findUserByProviderTypeAndAccount(OauthAttributes.KAKAO.getProvider(), info.account())
			.ifPresentOrElse(user -> {
				try {
					userQueryRepository.deleteByUserId(user.getId());
					jwtRedisRepository.deleteByUserId(user.getId());
				} catch (Exception ex) {
					sendKakaoCallbackFailureAlert(ex, info);
				}
			}, () -> sendKakaoCallbackFailureAlert(new ApiException(AuthErrorCode.NOT_FOUND_USER), info));
	}

	private void sendKakaoCallbackFailureAlert(Exception ex, KakaoUnlinkCallbackInfo info) {
		slackAlert.sendKakaoCallbackFailure(ex, info.account(), info.refererType());
	}
}
