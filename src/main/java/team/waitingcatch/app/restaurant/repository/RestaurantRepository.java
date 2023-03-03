package team.waitingcatch.app.restaurant.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import team.waitingcatch.app.restaurant.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
	@Query("select r from Restaurant r where r.user.id = :userId")
	Optional<Restaurant> findByUserId(@Param("userId") Long userId);

	Page<Restaurant> findByNameContaining(String searchVal, Pageable pageable);

	// @Query(value = "select new team.waitingcatch.app.restaurant.dto.restaurant.SearchRestaurantJpaResponse("
	// 	+ "r.name, r.images, i.rate, r.searchKeywords, r.position, i.currentWaitingNumber, i.isLineupActiveStatus) "
	// 	+ "from Restaurant r join fetch RestaurantInfo i on r.id = i.restaurant.id "
	// 	+ "where r.searchKeywords like %:keyword% and r.isDeleted = false")
	// List<SearchRestaurantJpaResponse> findRestaurantsBySearchKeywordsContainingJpql(String keyword);

	// @Query(value = "select new team.waitingcatch.app.restaurant.dto.restaurant.RestaurantsWithin3kmRadiusJpaResponse("
	// 	+ "r.name, r.images, i.rate, r.searchKeywords, r.position, i.currentWaitingNumber, i.isLineupActiveStatus) "
	// 	+ "from Restaurant r join fetch RestaurantInfo i on r.id = i.restaurant.id "
	// 	+ "where (6371 * acos(cos(radians(:latitude)) * cos(radians(r.position.latitude)) "
	// 	+ "* cos(radians(r.position.longitude) - radians(:longitude)) + sin(radians(:latitude)) "
	// 	+ "* sin(radians(r.position.latitude)))) < 3")
	// List<RestaurantsWithin3kmRadiusJpaResponse> findRestaurantsByDistance(
	// 	@Param("latitude") double latitude, @Param("longitude") double longitude);

	//
	// @Query("select new team.waitingcatch.app.restaurant.dto.restaurant.RestaurantsWithin3kmRadiusJpaResponse("
	// 	+ "r.name, r.images, i.rate, r.searchKeywords, r.position, i.currentWaitingNumber, i.isLineupActiveStatus) "
	// 	+ "from Restaurant r join fetch RestaurantInfo i on r.id = i.restaurant.id "
	// 	+ "where ST_Distance_Sphere(Point(:longitude, :latitude), Point(r.position.longitude, r.position.latitude)) / 1000 < 3 "
	// 	+ "and r.isDeleted = false")
	// List<RestaurantsWithin3kmRadiusJpaResponse> findRestaurantsByDistance(
	// 	@Param("latitude") double latitude, @Param("longitude") double longitude);
}