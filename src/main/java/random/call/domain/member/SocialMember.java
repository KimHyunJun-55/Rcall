package random.call.domain.member;


import jakarta.persistence.*;
import lombok.*;
import random.call.domain.member.type.SocialType;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class SocialMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;

    @Column(nullable = true)
    private String socialId;

    private SocialType socialType;
}
