package team.waitingcatch.app.restaurant.service.requestseller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.waitingcatch.app.lineup.entity.WaitingNumber;
import team.waitingcatch.app.lineup.repository.WaitingNumberRepository;
import team.waitingcatch.app.restaurant.dto.requestseller.ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest;
import team.waitingcatch.app.restaurant.dto.requestseller.ApproveSignUpSellerResponse;
import team.waitingcatch.app.restaurant.dto.requestseller.ApproveSignUpSellerServiceRequest;
import team.waitingcatch.app.restaurant.dto.requestseller.ConnectCategoryRestaurantServiceRequest;
import team.waitingcatch.app.restaurant.dto.requestseller.DemandSignUpSellerServiceRequest;
import team.waitingcatch.app.restaurant.dto.requestseller.GetDemandSignUpSellerResponse;
import team.waitingcatch.app.restaurant.dto.requestseller.GetRequestSellerByRestaurantRequest;
import team.waitingcatch.app.restaurant.dto.requestseller.RejectSignUpSellerServiceRequest;
import team.waitingcatch.app.restaurant.entity.Restaurant;
import team.waitingcatch.app.restaurant.entity.RestaurantInfo;
import team.waitingcatch.app.restaurant.entity.SellerManagement;
import team.waitingcatch.app.restaurant.repository.RestaurantInfoRepository;
import team.waitingcatch.app.restaurant.repository.RestaurantRepository;
import team.waitingcatch.app.restaurant.repository.SellerManagementRepository;
import team.waitingcatch.app.restaurant.service.category.InternalCategoryService;
import team.waitingcatch.app.user.dto.CreateUserServiceRequest;
import team.waitingcatch.app.user.entitiy.User;
import team.waitingcatch.app.user.enums.UserRoleEnum;
import team.waitingcatch.app.user.repository.UserRepository;
import team.waitingcatch.app.user.service.InternalUserService;
import team.waitingcatch.app.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerManagementServiceImpl implements SellerManagementService, InternalSellerManagementService {
	private final SellerManagementRepository sellerManagementRepository;
	private final UserRepository userRepository;
	private final RestaurantRepository restaurantRepository;
	private final RestaurantInfoRepository restaurantInfoRepository;
	private final WaitingNumberRepository waitingNumberRepository;
	private final InternalCategoryService categoryService;

	private final InternalUserService internalUserService;
	private final UserService userService;
	private final JavaMailSender emailSender;

	@Value("${spring.mail.username}")
	private String smtpSenderEmail;

	//판매자 요청 등록 하는 메소드
	public void demandSignUpSeller(DemandSignUpSellerServiceRequest demandSignupSellerServiceRequest) {
		boolean user = userRepository.existsByUsername(demandSignupSellerServiceRequest.getUsername());
		if (user) {
			throw new IllegalArgumentException("해당 사용자가 존재합니다.");
		}
		//internalUserService._getUserByUsername(demandSignupSellerServiceRequest.getUsername());
		//internalUserService._getUserByEmail(demandSignupSellerServiceRequest.getEmail());

		SellerManagement sellerManagement = new SellerManagement(demandSignupSellerServiceRequest);
		sellerManagementRepository.save(sellerManagement);
	}

	//판매자 요청 리스트 조회
	@Transactional(readOnly = true)
	public List<GetDemandSignUpSellerResponse> getDemandSignUpSellers() {
		List<SellerManagement> sellerManagement = sellerManagementRepository.findAll();
		return sellerManagement.stream().map(GetDemandSignUpSellerResponse::new).collect(Collectors.toList());
	}

	public ApproveSignUpSellerResponse approveSignUpSeller(ApproveSignUpSellerServiceRequest
		approveSignUpSellerServiceRequest) {
		SellerManagement sellerManagement = sellerManagementRepository.findById(
				approveSignUpSellerServiceRequest.getId())
			.orElseThrow(() -> new IllegalArgumentException("Not found request seller sign-up"));

		sellerManagement.checkReject();
		sellerManagement.checkApprove();

		sellerManagement.approveUpdateStatus();
		//비밀번호
		String uuidPassword = UUID.randomUUID().toString().substring(1, 8);
		//회원가입
		CreateUserServiceRequest
			userCreateServiceRequest
			= new CreateUserServiceRequest(UserRoleEnum.SELLER, sellerManagement.getName(), sellerManagement.getEmail(),
			sellerManagement.getUsername(), uuidPassword, null, sellerManagement.getPhoneNumber());
		userService.createUser(userCreateServiceRequest);

		User seller = internalUserService._getUserByUsername(sellerManagement.getUsername());
		// 레스토랑 만들기
		List<Long> categoryIds = sellerManagement.getCategories().stream()
			.map(Long::parseLong)
			.collect(Collectors.toList());

		List<String> categoryNames = categoryService._getCategoryNames(categoryIds);
		String searchKeywords = "";
		for (String categoryName : categoryNames) {
			searchKeywords += categoryName + " ";
		}

		ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest
			approveSignUpSellerManagementEntityPassToRestaurantEntityRequest
			= new ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest(sellerManagement, seller,
			searchKeywords);

		Restaurant restaurant = new Restaurant(approveSignUpSellerManagementEntityPassToRestaurantEntityRequest);
		restaurantRepository.save(restaurant);

		ConnectCategoryRestaurantServiceRequest serviceRequest = new ConnectCategoryRestaurantServiceRequest(restaurant,
			sellerManagement.getCategories());
		categoryService._connectCategoryRestaurant(serviceRequest);

		RestaurantInfo restaurantInfo = new RestaurantInfo(restaurant);
		restaurantInfoRepository.save(restaurantInfo);

		WaitingNumber waitingNumber = WaitingNumber.createWaitingNumber(restaurant);
		waitingNumberRepository.save(waitingNumber);

		// 저장된 번호를 유저에게 메일로 전달한다.
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(smtpSenderEmail);
		message.setTo(sellerManagement.getEmail());
		message.setSubject("WaitingCatching 판매자 동록 승인 메일");
		message.setText("안녕하세요. WaitingCatching 판매자 동록 승인 메일 입니다. 회원님의 아이디 및 임시 비밀번호는 " +
			"ID: " + sellerManagement.getUsername() + "," + uuidPassword + "입니다."
			+ "\n로그인 후에 비밀번호를 변경을 해주세요.");
		emailSender.send(message);
		return new ApproveSignUpSellerResponse(sellerManagement.getUsername(), uuidPassword);
	}

	public void rejectSignUpSeller(RejectSignUpSellerServiceRequest rejectSignUpSellerServiceRequest) {
		SellerManagement sellerManagement = sellerManagementRepository.findById(
				rejectSignUpSellerServiceRequest.getId())
			.orElseThrow(() -> new IllegalArgumentException("Not found request seller sign-up"));
		sellerManagement.checkReject();
		sellerManagement.checkApprove();
		sellerManagement.rejectUpdateStatus();

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(smtpSenderEmail);
		message.setTo(sellerManagement.getEmail());
		message.setSubject("WaitingCatching 판매자 동록 거절 메일");
		message.setText("안녕하세요. WaitingCatching 판매자 동록 거절 메일 입니다. 회원님의 정보에 오류가"
			+ "있는것으로 판단되므로 확인하고 다시 재신청 부탁드리겠습니다. 감사합니다.");
		emailSender.send(message);
	}

	@Override
	@Transactional(readOnly = true)
	public GetDemandSignUpSellerResponse getRequestSellerByRestaurant(
		GetRequestSellerByRestaurantRequest getRequestSellerByRestaurantRequest) {
		SellerManagement sellerManagement = sellerManagementRepository.findTopByUsernameAndEmailOrderByCreatedDateDesc(
			getRequestSellerByRestaurantRequest.getRequestSellerName(), getRequestSellerByRestaurantRequest.getEmail());
		return new GetDemandSignUpSellerResponse(sellerManagement);
	}

}