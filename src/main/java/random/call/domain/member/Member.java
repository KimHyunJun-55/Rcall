package random.call.domain.member;

import jakarta.persistence.*;
import lombok.*;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.member.dto.MemberRequest;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MBTI;
import random.call.domain.member.type.MemberType;
import random.call.global.encrypt.CryptoConverter;
import random.call.global.timeStamped.Timestamped;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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
    @Setter
    private String password;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false)
    private String phoneNumber;

    @Convert(converter = CryptoConverter.class)
    @Column(nullable = false)
    private String birthDate;

    @Embedded
    private DeviceInfo deviceInfo;

    @Column(nullable = false,unique = true)
    private String nickname;

    @Column(nullable = true)
    private String statusMessage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    @Builder.Default
    private Boolean isSubscriber =false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberType memberType= MemberType.STANDARD;

    private String profileImage;

    private String location;

    private Integer age;

    @ElementCollection
    private List<String> interest;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
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

    public static int calculateAge(String birthDate) {
        LocalDate birth = LocalDate.parse(birthDate, DateTimeFormatter.BASIC_ISO_DATE);
        return Period.between(birth, LocalDate.now()).getYears();
    }



}
