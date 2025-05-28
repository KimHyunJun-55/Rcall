package random.call.domain.member.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.member.Member;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendProfileResponseDTO {
    private Long id;
    private String nickname;
    private String profileImage;
    private String location;
    private List<String> interests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime requestDate;

    public FriendProfileResponseDTO(Member member,LocalDateTime time){
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.profileImage = member.getProfileImage();
        this.location = "KOREA";
        this.interests = member.getInterest();
        this.requestDate = time;

    }
}
