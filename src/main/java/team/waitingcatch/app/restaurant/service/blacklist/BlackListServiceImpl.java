package team.waitingcatch.app.restaurant.service.blacklist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.waitingcatch.app.restaurant.dto.blacklist.CreateBlackListInternalServiceRequest;
import team.waitingcatch.app.restaurant.dto.blacklist.DeleteUserBlackListByRestaurantServiceRequest;
import team.waitingcatch.app.restaurant.dto.blacklist.GetBlackListByRestaurantIdServiceRequest;
import team.waitingcatch.app.restaurant.dto.blacklist.GetBlackListResponse;
import team.waitingcatch.app.restaurant.entity.BlackList;
import team.waitingcatch.app.restaurant.entity.Restaurant;
import team.waitingcatch.app.restaurant.repository.BlackListRepository;
import team.waitingcatch.app.user.entitiy.User;

@Service
@RequiredArgsConstructor
@Transactional
public class BlackListServiceImpl implements BlackListService, InternalBlackListService {
	private final BlackListRepository blackListRepository;

	public void _createBlackList(
		Restaurant restaurant, User user) {
		CreateBlackListInternalServiceRequest createBlackListInternalServiceRequest
			= new CreateBlackListInternalServiceRequest(restaurant, user);
		BlackList blackList = new BlackList(createBlackListInternalServiceRequest);
		blackListRepository.save(blackList);
	}

	public void deleteUserBlackListByRestaurant(
		DeleteUserBlackListByRestaurantServiceRequest deleteUserBlackListByRestaurantServiceRequest) {
		BlackList blackList = blackListRepository.findByUser_IdAndRestaurant_User_Username(
			deleteUserBlackListByRestaurantServiceRequest.getUserId(),
			deleteUserBlackListByRestaurantServiceRequest.getSellerName()
		).orElseThrow(() -> new IllegalArgumentException("Not found blacklist user"));
		blackList.checkDeleteStatus();
		blackList.deleteSuccess();
	}

	@Transactional(readOnly = true)
	public List<GetBlackListResponse> getBlackListByRestaurantIdRequest(
		GetBlackListByRestaurantIdServiceRequest getBlackListByRestaurantIdServiceRequest) {
		List<BlackList> blackList = blackListRepository.findAllByRestaurant_Id(
			getBlackListByRestaurantIdServiceRequest.getRestaurantId());
		return blackList.stream().map(GetBlackListResponse::new).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<GetBlackListResponse> getBlacklist() {
		return blackListRepository.findAllByIsDeletedFalse().stream()
			.map(GetBlackListResponse::new)
			.collect(Collectors.toList());
	}
}
