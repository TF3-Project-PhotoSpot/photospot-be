package com.tf4.photospot.auth.util;

import java.util.Random;

public class NicknameGenerator {

	public static final String characters = "abcdefghijklmnopqrstuvwxyz0123456789";

	public static String generatorRandomNickname() {
		StringBuilder randomNickname = new StringBuilder();

		Random random = new Random();
		for (int i = 0; i < 8; i++) {
			int index = random.nextInt(characters.length());
			char randomChar = characters.charAt(index);
			randomNickname.append(randomChar);
		}

		return randomNickname.toString();

	}
}
