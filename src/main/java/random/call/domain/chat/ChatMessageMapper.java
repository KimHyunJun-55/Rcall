package random.call.domain.chat;

import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.entity.ChatMessage;

// Mapper 클래스 또는 정적 메서드 추가
public class ChatMessageMapper {
    public static ChatMessage toEntity(ChatMessageDto dto) {
        return ChatMessage.builder()
                .sender(dto.getSender())
                .content(dto.getContent())
                .roomId(dto.getRoomId())
                .build();
    }
}