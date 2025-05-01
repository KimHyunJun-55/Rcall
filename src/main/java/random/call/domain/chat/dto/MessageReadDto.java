package random.call.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageReadDto {
    private Long messageId;
    private String reader;
}