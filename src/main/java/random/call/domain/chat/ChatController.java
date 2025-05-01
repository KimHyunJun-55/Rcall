package random.call.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.dto.MessageReadDto;
import random.call.domain.chat.entity.ChatMessage;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        ChatMessage message = ChatMessage.builder()
                .sender(username)
                .content(dto.getContent())
                .roomId(dto.getRoomId())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        ChatMessageDto response = convertToDto(saved);
        messagingTemplate.convertAndSend("/topic/" + dto.getRoomId(), response);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/{roomId}")
    public ChatMessageDto addUser(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", dto.getSender());
        headerAccessor.getSessionAttributes().put("roomId", dto.getRoomId());
        return dto;
    }

    @MessageMapping("/chat.readMessage")
    public void readMessage(@Payload MessageReadDto readDto) {
        chatMessageRepository.findById(readDto.getMessageId()).ifPresent(message -> {
            // 읽음 처리 로직 (예: 읽은 사용자 목록 업데이트)
            messagingTemplate.convertAndSendToUser(
                    readDto.getReader(),
                    "/queue/read-receipts",
                    Map.of("messageId", readDto.getMessageId())
            );
        });
    }

    //createdAt넣어주기
    private ChatMessageDto convertToDto(ChatMessage entity) {
        return ChatMessageDto.builder()
                .sender(entity.getSender())
                .content(entity.getContent())
                .roomId(entity.getRoomId())
                .build();
    }
}

