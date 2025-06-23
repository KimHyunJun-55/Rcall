package random.call.domain.member;


import jakarta.persistence.*;
import lombok.*;
import random.call.domain.member.type.SocialType;
import random.call.global.encrypt.CryptoConverter;

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
    @Convert(converter = CryptoConverter.class)
    private String socialId;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;
}
