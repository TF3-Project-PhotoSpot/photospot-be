package com.tf4.photospot.auth.util;

import org.apache.commons.lang3.RandomStringUtils;

public class NicknameGenerator {
	public static String generateRandomNickname() {
		int randomCnt = (int)(Math.random() * 3) + 4;
		String randomStr = RandomStringUtils.randomAlphabetic(randomCnt);
		String randomNum = RandomStringUtils.randomNumeric(4);
		return randomStr + randomNum;
	}
}
