package random.call.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatMessageDto {
    private String sender;
    private String content;
    private String roomId;
}