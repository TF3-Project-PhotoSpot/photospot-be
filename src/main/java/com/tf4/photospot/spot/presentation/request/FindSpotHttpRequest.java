package com.tf4.photospot.spot.presentation.request;

import org.hibernate.validator.constraints.Range;

public record FindSpotHttpRequest(
	@Range(max = 10, message = "미리보기 사진은 1~10장만 가능합니다.")
	Integer postPreviewCount,
	@Range(max = 5, message = "태그 통계는 1~5개만 가능합니다.")
	Integer mostPostTagCount,
	Boolean distance
) {
	public FindSpotHttpRequest {
		if (postPreviewCount == null) {
			postPreviewCount = 5;
		}
		if (mostPostTagCount == null) {
			mostPostTagCount = 3;
		}
		if (distance == null) {
			distance = true;
		}
	}

	public boolean requireDistance() {
		return distance;
	}
}
