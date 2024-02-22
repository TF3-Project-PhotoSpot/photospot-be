package com.tf4.photospot.spot.domain;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SpotTest {

	@Test
	public void incrementPostCount() {
		// given
		Spot spot = createSpot("주소", createPoint(), 5L);

		// when
		spot.incPostCount();

		// then
		assertThat(spot.getPostCount()).isEqualTo(6L);
	}

	@Test
	public void decrementPostCount() {
		// given
		Spot spot = createSpot("주소", createPoint(), 5L);

		// when
		spot.decPostCount();

		// then
		assertThat(spot.getPostCount()).isEqualTo(4L);
	}
}
