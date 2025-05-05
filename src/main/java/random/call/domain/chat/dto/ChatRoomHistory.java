package random.call.domain.chat.dto;


import lombok.Builder;
import lombok.Data;
import random.call.domain.member.Member;

@Builder
@Data
public class ChatRoomHistory {

    private Long RoomId;
    private String memberNickname;
    private String lastMessage;
    private String lastTime;
}
