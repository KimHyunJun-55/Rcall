package random.call.domain.memberSubscription;

import jakarta.persistence.*;
import random.call.domain.member.Member;

import java.time.LocalDate;

@Entity
@Table(name = "member_call_quota")
public class MemberCallUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "call_count", nullable = false)
    private int callCount; // 0~2

    @Column(name = "call_minutes", nullable = false)
    private int callMinutes; // 누적 분 (0~30)
}
