package random.call.domain.chat;

import jakarta.persistence.EntityNotFoundException;
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
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.chat.repository.ChatMessageRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.global.jwt.JwtUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    private final ChatRoomRepository chatRoomRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        // 1. 인증 처리
        String token = extractTokenFromHeader(headerAccessor);
        Long userId = jwtUtil.getMemberIdToToken(token);
        String nickname = jwtUtil.getNicknameToToken(token);
//        ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(()->new EntityNotFoundException("not found chatRoom"))    ;

        // 2. 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .senderId(userId)
                .content(dto.getContent())
                .roomId(dto.getRoomId())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // 3. 프론트엔드 요구사항에 맞는 응답 구성
        ChatMessageDto response = ChatMessageDto.builder()
                .id(saved.getId().toString())
                .senderId(saved.getSenderId())
                .sender(nickname)
                .content(saved.getContent())
                .roomId(saved.getRoomId())
                .createdAt(saved.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .tempId(dto.getTempId()) // 클라이언트의 임시 ID 전달
                .build();

        messagingTemplate.convertAndSend("/topic/chat/room/" + dto.getRoomId(), response);
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
//    private ChatMessageDto convertToDto(ChatMessage entity) {
//        return ChatMessageDto.builder()
//                .sender(entity.getSender())
//                .content(entity.getContent())
//                .roomId(entity.getRoomId())
//                .build();
//    }
    private String extractTokenFromHeader(SimpMessageHeaderAccessor headerAccessor) {
        List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        String authHeader = authHeaders.get(0);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

