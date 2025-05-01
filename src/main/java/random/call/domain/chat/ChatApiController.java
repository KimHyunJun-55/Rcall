package random.call.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable String roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {

        List<ChatMessage> messages = after == null ?
                chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId) :
                chatMessageRepository.findRecentMessages(roomId, after);

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private ChatMessageDto convertToDto(ChatMessage entity) {
        // 구현 생략 (위와 동일)

        return null;
        
    }
}