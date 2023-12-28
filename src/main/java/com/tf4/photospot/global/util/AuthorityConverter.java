package com.tf4.photospot.global.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthorityConverter {

	private static final String DELIMITER = ",";

	public static List<GrantedAuthority> convertStringToGrantedAuthority(String authorities) {
		String[] authorityArr = authorities.split(DELIMITER);
		return Arrays.stream(authorityArr)
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
	}

	public static String convertGrantedAuthoritiesToString(Collection<? extends GrantedAuthority> authorities) {
		Set<String> authoritySet = new HashSet<>();
		for (GrantedAuthority authority : authorities) {
			authoritySet.add(authority.getAuthority());
		}
		return String.join(DELIMITER, authoritySet);
	}
}
