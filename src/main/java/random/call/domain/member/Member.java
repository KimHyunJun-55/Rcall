package random.call.domain.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MBTI;
import random.call.global.timeStamped.Timestamped;
import java.util.List;
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false,unique = true)
    private String nickname;

    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    private String profileImage;

    private List<String> interest;

    public void updateNickname(String nickname) {
        this.nickname=nickname;
    }
}
