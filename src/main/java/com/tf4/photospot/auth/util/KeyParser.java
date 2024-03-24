package com.tf4.photospot.auth.util;

import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.util.StringUtils;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

public class KeyParser {
	public static PrivateKey getPrivateKey(String key) {
		if (!StringUtils.hasText(key)) {
			throw new ApiException(AuthErrorCode.EMPTY_PRIVATE_KEY);
		}
		try {
			PEMParser pemParser = new PEMParser(new StringReader(convertNewLines(key)));
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
			PrivateKeyInfo object = (PrivateKeyInfo)pemParser.readObject();
			return converter.getPrivateKey(object);
		} catch (IOException ex) {
			throw new ApiException(AuthErrorCode.INVALID_PRIVATE_KEY);
		}
	}

	private static String convertNewLines(String key) {
		return key.replace("\\n", "\n");
	}
}
