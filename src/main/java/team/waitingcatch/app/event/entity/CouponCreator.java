package team.waitingcatch.app.event.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.waitingcatch.app.event.enums.CouponRoleEnum;

import javax.persistence.*;
import java.time.LocalDateTime;

//
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponCreator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_creator_id")
    private Long id;

    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int discount_price;

    @Column(nullable = false)
    private CouponRoleEnum discountType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime expireDate;
}
