package team.waitingcatch.app.event.service.couponcreator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.waitingcatch.app.event.dto.couponcreator.CreateAdminCouponCreatorServiceRequest;
import team.waitingcatch.app.event.dto.couponcreator.CreateSellerCouponCreatorServiceRequest;
import team.waitingcatch.app.event.dto.couponcreator.UpdateAdminCouponCreatorServiceRequest;
import team.waitingcatch.app.event.dto.couponcreator.UpdateSellerCouponCreatorServiceRequest;
import team.waitingcatch.app.event.entity.CouponCreator;
import team.waitingcatch.app.event.entity.Event;
import team.waitingcatch.app.event.repository.CouponCreatorRepository;
import team.waitingcatch.app.event.repository.EventServiceRepository;
import team.waitingcatch.app.event.service.event.InternalEventService;
import team.waitingcatch.app.restaurant.entity.Restaurant;
import team.waitingcatch.app.restaurant.repository.RestaurantRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponCreatorServiceImpl implements CouponCreatorService, InternalCouponCreatorService {

	private final EventServiceRepository eventServiceRepository;
	private final CouponCreatorRepository couponCreatorRepository;
	private final RestaurantRepository restaurantRepository;
	private final InternalEventService internalEventService;

	//광역 이벤트 쿠폰생성자를 생성한다.
	@Override
	public String createAdminCouponCreator(
		CreateAdminCouponCreatorServiceRequest createAdminCouponCreatorServiceRequest) {
		Event events = internalEventService._getEventFindById(createAdminCouponCreatorServiceRequest.getEventId());
		CouponCreator couponCreator = new CouponCreator(createAdminCouponCreatorServiceRequest, events);
		couponCreatorRepository.save(couponCreator);
		return "해당 이벤트에 쿠폰 생성자를 추가하였습니다.";
	}

	//레스토랑 이벤트 쿠폰생성자를 생성한다
	@Override
	public String createSellerCouponCreator(
		CreateSellerCouponCreatorServiceRequest createSellerCouponCreatorServiceRequest) {
		Restaurant restaurant = internalEventService._getRestaurantFindByUsername(
			createSellerCouponCreatorServiceRequest.getUsername());
		Event ev = internalEventService._getEventFindById(createSellerCouponCreatorServiceRequest.getEventId());

		Event events = eventServiceRepository.findByIdAndRestaurant(
			createSellerCouponCreatorServiceRequest.getEventId(),
			restaurant).orElseThrow(
			() -> new IllegalArgumentException("매장에 해당 이벤트가 존재하지 않습니다.")
		);

		CouponCreator couponCreator = new CouponCreator(createSellerCouponCreatorServiceRequest, events);
		couponCreatorRepository.save(couponCreator);
		return "해당 이벤트에 쿠폰 생성자를 추가하였습니다.";
	}

	//광역 이벤트 쿠폰생성자를 수정한다.
	@Override
	public String updateAdminCouponCreator(
		UpdateAdminCouponCreatorServiceRequest updateAdminCouponCreatorServiceRequest) {
		Event events = internalEventService._getEventFindById(updateAdminCouponCreatorServiceRequest.getEventId());
		CouponCreator couponCreators = _getCouponCreatorFindById(updateAdminCouponCreatorServiceRequest.getCreatorId());

		couponCreators.updateAdminCouponCreator(updateAdminCouponCreatorServiceRequest);
		return "광역 이벤트 쿠폰생성자를 수정하였습니다.";
	}

	//레스토랑 이벤트 쿠폰생성자를 수정한다.
	@Override
	public String updateSellerCouponCreator(
		UpdateSellerCouponCreatorServiceRequest updateSellerCouponCreatorServiceRequest) {
		Restaurant restaurant = internalEventService._getRestaurantFindByUsername(
			updateSellerCouponCreatorServiceRequest.getUsername());
		Event ev = internalEventService._getEventFindById(updateSellerCouponCreatorServiceRequest.getEventId());

		Event events = eventServiceRepository.findByIdAndRestaurant(
			updateSellerCouponCreatorServiceRequest.getEventId(),
			restaurant).orElseThrow(
			() -> new IllegalArgumentException("매장에 해당 이벤트가 존재하지 않습니다.")
		);

		CouponCreator couponCreators = _getCouponCreatorFindById(
			updateSellerCouponCreatorServiceRequest.getCreatorId());

		couponCreators.updateSellerCouponCreator(updateSellerCouponCreatorServiceRequest);
		return "레스토랑 이벤트 쿠폰생성자를 수정하였습니다.";
	}

	@Override
	@Transactional(readOnly = true)
	public CouponCreator _getCouponCreatorFindById(Long id) {
		CouponCreator couponCreator = couponCreatorRepository.findById(id)
			.orElseThrow(
				() -> new IllegalArgumentException("쿠폰 생성자를 찾을수 없습니다.")
			);
		return couponCreator;
	}
}
