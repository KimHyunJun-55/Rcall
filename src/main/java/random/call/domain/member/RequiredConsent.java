package random.call.domain.member;


import jakarta.persistence.*;
import lombok.*;
import random.call.global.timeStamped.Timestamped;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RequiredConsent extends Timestamped {  // 또는 Timestamped

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Column(nullable = false)
    private Boolean privacyPolicy;  // 개인정보 처리방침 동의

    @Column(nullable = false)
    private Boolean termsOfService;  // 서비스 이용약관 동의

    @Column(nullable = false)
    @Builder.Default
    private Boolean ageOver14 =false;  // 만 14세 이상 확인

    @Column(nullable = false)
    @Builder.Default
    private Boolean marketingConsent=false;  // 선택적 마케팅 수신 동의

}