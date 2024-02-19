package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.auth.util.NicknameGenerator;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.request.LoginUserInfo;
import com.tf4.photospot.user.application.response.OauthLoginResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AppleService appleService;
	private final KakaoService kakaoService;

	private static final int NICKNAME_GENERATOR_RETRY_MAX = 5;

	@Transactional
	public OauthLoginResponse kakaoLogin(String accessToken, String id) {
		return oauthLogin(OauthAttributes.KAKAO.getProvider(), validateAndGetKakaoUserInfo(accessToken, id));
	}

	@Transactional
	public OauthLoginResponse appleLogin(String identityToken, String nonce) {
		return oauthLogin(OauthAttributes.APPLE.getProvider(), validateAndGetAppleUserInfo(identityToken, nonce));
	}

	public OauthLoginResponse oauthLogin(String provider, String account) {
		return userRepository.findUserByProviderTypeAndAccount(provider, account)
			.map(findUser -> OauthLoginResponse.from(true, findUser))
			.orElseGet(() -> OauthLoginResponse.from(false,
				userRepository.save(new LoginUserInfo(provider, account).toUser(generateNickname()))));
	}

	// Todo : 클라이언트에서 accessToken 전달 시 PREFIX도 함께 오는지 확인 후 수정
	private String validateAndGetKakaoUserInfo(String accessToken, String id) {
		return kakaoService.getTokenInfo(accessToken, id).account();
	}

	private String validateAndGetAppleUserInfo(String identityToken, String nonce) {
		return appleService.getTokenInfo(identityToken, nonce).account();
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
		jwtService.validRefreshToken(userId, refreshToken);
		User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		return new ReissueTokenResponse(jwtService.issueAccessToken(user.getId(), user.getRole().getType()));
	}
}
