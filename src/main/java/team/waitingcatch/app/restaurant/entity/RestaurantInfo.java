package team.waitingcatch.app.restaurant.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.waitingcatch.app.common.entity.TimeStamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantInfo extends TimeStamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "restaurant_info_id")
	private Long id;

	@OneToOne
	@JoinColumn(name = "restaurant_id", nullable = false)
	private Restaurant restaurant;

	@Column(nullable = false)
	private String openTime;

	@Column(nullable = false)
	private String closeTime;

	@Column(nullable = false)
	private float rate = 0;

	@Column(nullable = false)
	private int totalReview = 0;

	@Column(nullable = false)
	private int totalLineup;

	@Column(nullable = false)
	private boolean isLineupActiveStatus;

	@Column(nullable = false)
	private int currentWaitingNumber = 0;

	// 리뷰 작성시 해당 레스토랑의 평균 별점을 갱신한다.
	public void setAverageRate(float rate) {
		this.rate = rate;
		this.totalReview += 1;
	}

	// 줄서기 발생시 해당 레스토랑의 누적 대기 횟수와 현재 대기인원 수를 더해준다.
	public void addLineupCount() {
		this.currentWaitingNumber += 1;
		this.totalLineup += 1;
	}

	// 줄서기 취소, 완료시 해당 레스토랑의 현재 대기인원 수를 차감한다.
	public void subtractLineupCount() {
		this.currentWaitingNumber -= 1;
	}
}
