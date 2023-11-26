package com.tf4.photospot.spot.infrastructure.dto.kakao;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record KakaoAddressConvertResponse(
	Meta meta,
	List<Document> documents
) {
	public Optional<String> findAddressName() {
		if (meta.totalCount() == 0 || documents.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(documents.get(0))
			.map(Document::roadAddress)
			.map(KakaoRoadAddress::addressName);
	}
}
