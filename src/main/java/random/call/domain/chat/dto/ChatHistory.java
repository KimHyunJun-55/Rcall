package random.call.domain.chat.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {
    private Long id;          // 메시지 고유 ID (DB에서 생성)
    private Long senderId;      // 발신자 실제 ID
    private String content;     // 메시지 내용
    private Long roomId;      // 채팅방 ID
    private String createdAt;
}
