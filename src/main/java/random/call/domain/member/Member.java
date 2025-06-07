package random.call.domain.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.member.dto.MemberRequest;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MBTI;
import random.call.global.timeStamped.Timestamped;

import java.util.ArrayList;
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

    @Column(nullable = true)
    private String statusMessage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    private String profileImage;

    private String location;

    @ElementCollection
    private List<String> interest;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatParticipant> chatRooms = new ArrayList<>();



    public void updateNickname(String nickname) {
        this.nickname=nickname;
    }
    public void updateMbti(MBTI mbti) {
        this.mbti=mbti;
    }

    public void updateMessage(String message) {
        this.statusMessage=message;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImage=imageUrl;
    }



    public void updateInterests(MemberRequest.MemberInterests interests) {
        this.interest.clear();
        this.interest =interests.interests();
    }
    public void updateQuestionAnswers(List<QuestionAnswer> newQuestionAnswers) {
    }
}
