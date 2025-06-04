package random.call.domain.memberSubscription;

import jakarta.persistence.*;
import random.call.domain.member.Member;
import random.call.global.timeStamped.Timestamped;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_subscription")
public class MemberSubscription extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 구독 종류 (예: FREE, BASIC, PREMIUM, VIDEO)
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    // 구독 상태 (예: ACTIVE, EXPIRED, CANCELLED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    // 구독 시작일/만료일
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expireDate;

    // iOS/Android 구분
    @Column(name = "platform", length = 10)
    private String platform;

    // 마지막 영수증 검증 시간
    @Column(name = "last_verified")
    private LocalDateTime lastVerified;

    // 원한다면: 현재 사용 가능한 누적 통화 시간/영상 시간
    @Column(name = "available_call_minutes")
    private Integer availableCallMinutes;

    @Column(name = "available_video_minutes")
    private Integer availableVideoMinutes;


    // === enum 예시 ===
    public enum PlanType {
        FREE, BASIC, PREMIUM, VIDEO
    }

    public enum SubscriptionStatus {
        ACTIVE, EXPIRED, CANCELLED
    }

    // ...getter, setter, builder 등
}
