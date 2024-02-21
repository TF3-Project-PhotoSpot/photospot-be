package com.tf4.photospot.auth.application.response;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApplePublicKeyResponse {

	private List<Key> keys;

	@Setter
	@Getter
	public static class Key {
		private String kty;

		private String kid;

		private String use;

		private String alg;

		@JsonProperty("n")
		private String modulus;

		@JsonProperty("e")
		private String exponent;
	}

	public Optional<ApplePublicKeyResponse.Key> getMatchedKeyBy(String kid, String alg) {
		return this.keys.stream()
			.filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
			.findFirst();
	}
}
