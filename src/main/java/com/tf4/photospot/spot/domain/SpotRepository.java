package com.tf4.photospot.spot.domain;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.spot.application.response.NearbySpotResponse;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
	Optional<Spot> findByCoord(Point coord);

	@Query("""
		select new com.tf4.photospot.spot.application.response.NearbySpotResponse(s.id, s.coord)
		from Spot s
		where st_contains(st_buffer(:coord, :radius), s.coord)
		""")
	List<NearbySpotResponse> findNearbySpots(@Param("coord") Point coord, @Param("radius") Integer radius);
}
