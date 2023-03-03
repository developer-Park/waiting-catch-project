package team.waitingcatch.app.restaurant.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.waitingcatch.app.common.Position;
import team.waitingcatch.app.common.entity.TimeStamped;
import team.waitingcatch.app.common.util.StringListConverter;
import team.waitingcatch.app.restaurant.dto.requestseller.ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.SaveDummyRestaurantRequest;
import team.waitingcatch.app.restaurant.dto.restaurant.UpdateRestaurantEntityRequest;
import team.waitingcatch.app.user.entitiy.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends TimeStamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "restaurant_id")
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Convert(converter = StringListConverter.class)
	private final List<String> imagePaths = new ArrayList<>();

	@Embedded
	private Position position;

	@Column(nullable = false, length = 10)
	private String zipCode;

	@Column(nullable = false, length = 50)
	private String address;

	@Column(nullable = false, length = 30)
	private String detailAddress;

	// @Embedded
	// private Address address;

	@Column(nullable = false, length = 20)
	private String phoneNumber;

	@Column(nullable = false)
	private boolean isDeleted;

	@Convert(converter = StringListConverter.class)
	@Column(name = "search_keywords")
	private List<String> searchKeywords = new ArrayList<>();

	@Column(nullable = false, length = 255)
	private String description;

	@Column(nullable = false, length = 100)
	private int capacity;

	@Column(nullable = false, length = 12)
	private String businessLicenseNo;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// @Column(nullable = false)
	// private String category;

	public Restaurant(ApproveSignUpSellerManagementEntityPassToRestaurantEntityRequest entityRequest) {
		this.name = entityRequest.getRestaurantName();
		this.position = entityRequest.getPosition();
		this.zipCode = entityRequest.getZipCode();
		this.address = entityRequest.getAddress();
		this.detailAddress = entityRequest.getDetailAddress();
		// this.address = entityRequest.getAddress();
		this.phoneNumber = entityRequest.getPhoneNumber();
		this.isDeleted = false;
		this.searchKeywords = entityRequest.getSearchKeyWords();
		this.description = entityRequest.getDescription();
		this.businessLicenseNo = entityRequest.getBusinessLicenseNo();
		this.capacity = 0;
		this.user = entityRequest.getUser();
		// this.category = entityRequest.getCategories();
	}

	//dummy data
	public Restaurant(SaveDummyRestaurantRequest request) {
		this.name = request.getName();
		this.zipCode = request.getZipCode();
		this.address = request.getAddress();
		this.detailAddress = request.getDetailAddress();
		this.businessLicenseNo = String.valueOf(UUID.randomUUID()).substring(0, 12);
		this.capacity = 30;
		this.description = request.getName() + "은 한국 최고의 음식 입니다.";
		this.phoneNumber = request.getPhoneNumber();
		this.position = request.getPosition();
		// this.category = request.getCategory();
		this.user = request.getUser();
		this.searchKeywords = request.getSearchkeywords();
	}

	// public String getProvince() {
	// 	return this.getAddress().getProvince();
	// }
	//
	// public String getCity() {
	// 	return this.getAddress().getCity();
	// }
	//
	// public String getStreet() {
	// 	return this.getAddress().getStreet();
	// }

	public double getLatitude() {
		return this.getPosition().getLatitude();
	}

	public double getLongitude() {
		return this.getPosition().getLongitude();
	}

	public void deleteRestaurant() {
		if (this.isDeleted) {
			throw new IllegalArgumentException("해당 유저는 이미 삭제되었습니다.");
		} else {
			this.isDeleted = true;
		}
	}

	public void updateRestaurant(UpdateRestaurantEntityRequest updateRestaurantEntityRequest) {
		this.imagePaths.addAll(updateRestaurantEntityRequest.getImagePaths());
		this.phoneNumber = updateRestaurantEntityRequest.getPhoneNumber();
		this.capacity = updateRestaurantEntityRequest.getCapacity();
		this.description = updateRestaurantEntityRequest.getDescription();
	}
}