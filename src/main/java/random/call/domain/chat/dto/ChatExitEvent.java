package random.call.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatExitEvent {
    private String type;
    private boolean isActive;
    private Long matchUserId;
    private LocalDateTime exitedAt;
}