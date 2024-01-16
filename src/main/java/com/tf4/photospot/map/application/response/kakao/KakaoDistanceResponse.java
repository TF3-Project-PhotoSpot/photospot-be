package com.tf4.photospot.map.application.response.kakao;

import java.util.Collections;
import java.util.List;

public record KakaoDistanceResponse(
	List<Route> routes
) {
	public KakaoDistanceResponse {
		if (routes == null) {
			routes = Collections.emptyList();
		}
	}

	public Integer getDistance() {
		if (routes.isEmpty()) {
			return null;
		}
		return routes().get(0).summary.distance;
	}

	public record Route(
		Integer resultCode,
		Summary summary
	) {
		public Route {
			if (summary == null) {
				summary = new Summary(0, 0);
			}
		}

		public record Summary(
			int distance,
			int duration
		) {
		}
	}
}
