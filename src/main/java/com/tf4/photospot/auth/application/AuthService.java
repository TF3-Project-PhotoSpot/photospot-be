package com.tf4.photospot.auth.application;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.request.LoginUserInfo;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.util.NicknameGenerator;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AppleService appleService;

	private static final int NICKNAME_GENERATOR_RETRY_MAX = 5;

	@Transactional
	public OauthLoginUserResponse oauthLogin(String type, Map<String, String> identityInfo) {
		String provider = OauthAttributes.findByType(type).getProvider();
		String account = provider.equals(OauthAttributes.KAKAO.getProvider()) ? validateKakaoAccount() :
			validateAppleAccount(identityInfo);
		return userRepository.findUserByProviderTypeAndAccount(provider, account)
			.map(findUser -> OauthLoginUserResponse.from(true, findUser))
			.orElseGet(() -> OauthLoginUserResponse.from(false,
				userRepository.save(new LoginUserInfo(provider, account).toUser(generateNickname()))));
	}

	// Todo
	public String validateKakaoAccount() {
		return null;
	}

	public String validateAppleAccount(Map<String, String> identityInfo) {
		return appleService.getToken(identityInfo.get(SecurityConstant.IDENTITY_TOKEN_PARAM),
			identityInfo.get(SecurityConstant.NONCE_PARAM)).subject();
	}

	private boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	// Todo : 커스텀 예외
	private String generateNickname() {
		int count = 0;
		String generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		while (isNicknameDuplicated(generatedRandomNickname)) {
			if (count++ >= NICKNAME_GENERATOR_RETRY_MAX) {
				throw new RuntimeException();
			}
			generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		}
		return generatedRandomNickname;
	}

	public ReissueTokenResponse reissueToken(Long userId, String refreshToken) {
		jwtService.validRefreshToken(userId, refreshToken);
		User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		return new ReissueTokenResponse(jwtService.issueAccessToken(user.getId(), user.getRole().getType()));
	}
}
