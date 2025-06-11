package random.call.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.dto.MarkAsReadRequest;
import random.call.domain.match.MatchType;
import random.call.domain.match.service.ChatMatchService;
import random.call.domain.match.service.MatchService;
import random.call.global.jwt.JwtUtil;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final MatchService matchService;

    @MessageMapping("/matching/request")
    public void handleMatchingRequest(Message<?> incomingMessage) throws InterruptedException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);

        String token = jwtUtil.extractToken(accessor);
        var userId = jwtUtil.getMemberIdToToken(token);

        // 🕐 매칭 지연 (테스트용)
//        Thread.sleep(15_000); // 지연
        matchService.processMatching(userId, MatchType.CHAT);

    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        String token = extractTokenFromHeader(headerAccessor);
        Long userId = jwtUtil.getMemberIdToToken(token);
        String nickname = jwtUtil.getNicknameToToken(token);

        chatService.extracted(dto, userId, nickname);
    }



    @MessageMapping("/chat.addUser")
    @SendTo("/topic/{roomId}")
    public ChatMessageDto addUser(@Payload ChatMessageDto dto, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", dto.getSender());
        headerAccessor.getSessionAttributes().put("roomId", dto.getRoomId());
        return dto;
    }

    @MessageMapping("/chat.markAsRead")
    public void markAsRead(@Payload MarkAsReadRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String token = extractTokenFromHeader(headerAccessor);
        Long userId = jwtUtil.getMemberIdToToken(token);
        chatService.markMessagesAsRead(request. getRoomId(), userId,request.getMessageIds());
    }


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

