package random.call.domain.friendList.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.member.Member;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FriendResponseDTO {
    private Long id;
    private String nickname;
    private String statusMessage;
    private String profileImage;
    private String location;

    public FriendResponseDTO(Member member){
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.statusMessage = "상태메시지는 아직없어요";
        this.profileImage = member.getProfileImage();
//        this.location = member.getLocation();
        this.location = "KOREA";


    }

}
