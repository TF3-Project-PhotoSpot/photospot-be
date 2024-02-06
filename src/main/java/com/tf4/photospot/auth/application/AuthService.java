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

	@Transactional
	public OauthLoginUserResponse oauthLogin(String providerType, Map<String, String> identityInfo) {
		String account;
		if (providerType.equals(OauthAttributes.KAKAO.getType())) {
			account = identityInfo.get(SecurityConstant.ACCOUNT_PARAM);
		} else {
			account = getAppleAccount(identityInfo);
		}
		return userRepository.findUserByProviderTypeAndAccount(providerType, account)
			.map(findUser -> OauthLoginUserResponse.from(true, findUser))
			.orElseGet(() -> OauthLoginUserResponse.from(false,
				userRepository.save(new LoginUserInfo(providerType, account).toUser(generateNickname()))));
	}

	public String getAppleAccount(Map<String, String> identityInfo) {
		return appleService.getAppleId(identityInfo.get(SecurityConstant.IDENTITY_TOKEN_PARAM),
			identityInfo.get(SecurityConstant.NONCE_PARAM));
	}

	private boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	private String generateNickname() {
		String generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		while (isNicknameDuplicated(generatedRandomNickname)) {
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
