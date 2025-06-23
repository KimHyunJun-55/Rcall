package random.call.domain.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;          // 메시지 고유 ID (DB에서 생성)
    private Long targetId;      // 상대방 아이디
    private Long senderId;      // 발신자 실제 ID
    private String sender;      // 발신자 이름 (닉네임)
    private String content;     // 메시지 내용
    private Long roomId;      // 채팅방 ID
    private String createdAt;  // ISO 8601 형식의 생성 시간
    private String tempId;     // (선택) 클라이언트의 임시 ID
}