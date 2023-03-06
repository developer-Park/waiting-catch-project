package team.waitingcatch.app.restaurant.service.restaurant;

import static team.waitingcatch.app.common.enums.ImageDirectoryEnum.*;
import static team.waitingcatch.app.exception.ErrorCode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.waitingcatch.app.common.util.DistanceCalculator;
import team.waitingcatch.app.common.util.image.ImageUploader;
import team.waitingcatch.app.restaurant.dto.requestseller.ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.DeleteRestaurantByAdminServiceRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantBasicInfoResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantBasicInfoServiceRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantDetailedInfoResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantDetailedInfoServiceRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantsWithinRadiusJpaResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantsWithinRadiusResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.RestaurantsWithinRadiusServiceRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.SearchRestaurantJpaResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.SearchRestaurantServiceRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.SearchRestaurantsResponse;
import team.waitingcatch.app.restaurant.dto.restaurant.UpdateRestaurantEntityRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.UpdateRestaurantServiceRequest;
import team.waitingcatch.app.restaurant.entity.Restaurant;
import team.waitingcatch.app.restaurant.entity.RestaurantInfo;
import team.waitingcatch.app.restaurant.repository.RestaurantInfoRepository;
import team.waitingcatch.app.restaurant.repository.RestaurantRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService, InternalRestaurantService {
	private final RestaurantRepository restaurantRepository;
	private final RestaurantInfoRepository restaurantInfoRepository;
	private final ImageUploader imageUploader;
	private final DistanceCalculator distanceCalculator;

	@Override
	@Transactional(readOnly = true)
	public RestaurantBasicInfoResponse getRestaurantBasicInfo(RestaurantBasicInfoServiceRequest request) {
		Restaurant restaurant = _getRestaurantById(request.getRestaurantId());
		return new RestaurantBasicInfoResponse(restaurant);
	}

	@Override
	@Transactional(readOnly = true)
	public RestaurantDetailedInfoResponse getRestaurantDetailedInfo(RestaurantDetailedInfoServiceRequest request) {
		Restaurant restaurant = _getRestaurantById(request.getRestaurantId());
		RestaurantInfo restaurantInfo = restaurantInfoRepository.findByRestaurantId(restaurant.getId()).orElseThrow(
			() -> new IllegalArgumentException(NOT_FOUND_RESTAURANT.getMessage())
		);
		return new RestaurantDetailedInfoResponse(restaurant, restaurantInfo);
	}

	@Override
	@Transactional(readOnly = true)
	public Slice<SearchRestaurantsResponse> searchRestaurantsByKeyword(SearchRestaurantServiceRequest request) {
		Slice<SearchRestaurantJpaResponse> jpaResponses =
			restaurantInfoRepository.findRestaurantsBySearchKeywordsContaining(
				request.getId(), request.getKeyword(), request.getPageable()
			);

		List<SearchRestaurantsResponse> content = new ArrayList<>();
		for (SearchRestaurantJpaResponse response : jpaResponses) {
			double distance = distanceCalculator.distanceInKilometerByHaversine(
				request.getLongitude(),
				request.getLatitude(),
				response.getLongitude(),
				response.getLatitude()
			);
			content.add(new SearchRestaurantsResponse(response, distance));
		}

		return new SliceImpl<>(content, jpaResponses.getPageable(), jpaResponses.hasNext());
	}

	@Override
	@Transactional(readOnly = true)
	public Slice<RestaurantsWithinRadiusResponse> getRestaurantsWithinRadius(
		RestaurantsWithinRadiusServiceRequest request) {
		Slice<RestaurantsWithinRadiusJpaResponse> jpaResponses =
			restaurantInfoRepository.findRestaurantsByDistance(
				request.getId(),
				request.getLatitude(),
				request.getLongitude(),
				request.getDistance(),
				request.getPageable()
			);
		List<RestaurantsWithinRadiusResponse> content = new ArrayList<>();
		for (RestaurantsWithinRadiusJpaResponse response : jpaResponses) {
			double distance = distanceCalculator.distanceInKilometerByHaversine(
				request.getLongitude(),
				request.getLatitude(),
				response.getLongitude(),
				response.getLatitude()
			);
			content.add(new RestaurantsWithinRadiusResponse(response, distance));
		}

		return new SliceImpl<>(content, jpaResponses.getPageable(), jpaResponses.hasNext());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RestaurantResponse> getRestaurants(Pageable pageable) {
		Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);
		return new PageImpl<>(
			(restaurantRepository.findAll(pageable).getContent().stream().map(RestaurantResponse::new).collect(
				Collectors.toList())), pageable,
			restaurants.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RestaurantResponse> getRestaurantsByRestaurantName(String searchVal, Pageable pageable) {
		Page<Restaurant> restaurants = restaurantRepository.findByNameContaining(searchVal, pageable);
		return new PageImpl<>(
			(restaurantRepository.findByNameContaining(searchVal, pageable)
				.getContent()
				.stream()
				.map(RestaurantResponse::new)
				.collect(
					Collectors.toList())), pageable,
			restaurants.getTotalElements());
	}

	@Override
	public void deleteRestaurantByAdmin(DeleteRestaurantByAdminServiceRequest deleteRestaurantByAdminServiceRequest) {
		Restaurant restaurant = _getRestaurantById(deleteRestaurantByAdminServiceRequest.getRestaurantId());
		//String transferToString[] = restaurant.getImages().split(",");
		// for (int i = 0; i < transferToString.length; i++) {
		// 	s3Uploader.deleteS3(transferToString[i]);
		// }
		restaurant.deleteRestaurant();
	}
	// 현재 있는 것은

	//업데이트시 -> 현재 있는것은 1.있는것 2. 있는것 3. 새로 4.새로
	//업데이트시 -> 현재 있는것은 1.새로 2. 새로 3. 새로 4.새로
	@Override
	public void updateRestaurant(UpdateRestaurantServiceRequest serviceRequest) throws IOException {
		Restaurant restaurant = restaurantRepository.findByUserId(serviceRequest.getSellerId())
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT.getMessage()));

		RestaurantInfo restaurantInfo = restaurantInfoRepository.findById(restaurant.getId())
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT_INFO.getMessage()));

		List<String> imagePaths = imageUploader.uploadList(serviceRequest.getImages(), RESTAURANT.getValue());
		UpdateRestaurantEntityRequest updateRestaurantEntityRequest = new UpdateRestaurantEntityRequest(
			serviceRequest, imagePaths);

		restaurant.updateRestaurant(updateRestaurantEntityRequest);
		restaurantInfo.updateRestaurantInfo(updateRestaurantEntityRequest);
	}

	@Override
	public Restaurant _getRestaurantById(Long restaurantId) {
		return restaurantRepository.findById(restaurantId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT.getMessage()));
	}

	@Override
	public Restaurant _getRestaurantByUserId(Long userId) {
		return restaurantRepository.findByUserId(userId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT.getMessage()));
	}

	public RestaurantInfo _getRestaurantInfoByRestaurantId(Long restaurantId) {
		return restaurantInfoRepository.findByRestaurantId(restaurantId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT.getMessage()));
	}

	@Override
	public RestaurantInfo _getRestaurantInfoByUserId(Long userId) {
		return restaurantInfoRepository.findByUserId(userId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT_INFO.getMessage()));
	}

	@Override
	public RestaurantInfo _getRestaurantInfoByRestaurantIdWithRestaurant(Long id) {
		return restaurantInfoRepository.findByRestaurantId(id)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT_INFO.getMessage()));
	}

	@Override
	public void _createRestaurant(ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest request) {
		Restaurant restaurant = new Restaurant(request);
		restaurantRepository.save(restaurant);
		RestaurantInfo restaurantInfo = new RestaurantInfo(restaurant);
		restaurantInfoRepository.save(restaurantInfo);
	}

	public void _openLineup(Long restaurantId) {
		RestaurantInfo restaurantInfo = restaurantInfoRepository.findByRestaurantId(restaurantId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT_INFO.getMessage()));
		restaurantInfo.openLineup();
	}

	@Override
	public void _closeLineup(Long restaurantId) {
		RestaurantInfo restaurantInfo = restaurantInfoRepository.findByRestaurantId(restaurantId)
			.orElseThrow(() -> new NoSuchElementException(NOT_FOUND_RESTAURANT_INFO.getMessage()));
		restaurantInfo.closeLineup();
	}

	@Override
	public Restaurant _deleteRestaurantBySellerId(Long sellerId) {
		Restaurant restaurant = _getRestaurantByUserId(sellerId);
		RestaurantInfo restaurantInfo = _getRestaurantInfoByUserId(sellerId);

		restaurant.deleteRestaurant();
		restaurantInfo.deleteRestaurantInfo();

		return restaurant;
	}
}