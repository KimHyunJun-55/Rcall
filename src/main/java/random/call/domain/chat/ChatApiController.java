package random.call.domain.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.dto.ChatHistory;
import random.call.domain.chat.entity.ChatMessage;
import random.call.domain.chat.dto.ChatRoomHistory;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.chat.repository.ChatMessageRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.match.MatchType;
import random.call.domain.match.service.ChatMatchService;
import random.call.domain.match.service.MatchService;
import random.call.global.security.userDetails.JwtUserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatApiController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final MatchService matchService;

    @GetMapping("/count")
    public ResponseEntity<Integer> matchCount(){
        return ResponseEntity.ok(matchService.getCount(MatchType.CHAT));
    }


    @GetMapping("/{roomId}")
    public ResponseEntity<List<ChatHistory>> getChatRoomMessagesHistory(@PathVariable("roomId") Long roomId){
//        log.info("{} : 채팅방 내역조회========================",roomId);
//        ChatRoom chatRoom =chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("not found chatRoom"));

        List<ChatMessage> messages =chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        List<ChatHistory> messageDtos =messages.stream()
                .map(this::convertChatMessageToDto)
                .toList();

        return ResponseEntity.ok(messageDtos);

    }

    @GetMapping("/status/{roomId}")
    public ResponseEntity<Boolean> getChatRoomStatus(@PathVariable("roomId") Long roomId){
        ChatRoom chatRoom =chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("not found chatRoom"));
        return ResponseEntity.ok(chatRoom.isActive());

    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatRoomHistory>> getChatHistory(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){

        List<ChatRoomHistory> chatRoomHistories =chatService.getChatRoomHistory(jwtUserDetails.id());

        return ResponseEntity.ok(chatRoomHistories);

    }



    @GetMapping("/history/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {

        List<ChatMessage> messages = after == null ?
                chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId) :
                chatMessageRepository.findRecentMessages(roomId, after);

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelMatching(@AuthenticationPrincipal JwtUserDetails jwtUserDetails) {
        matchService.removeFromMatchingPool(jwtUserDetails.id());
        return ResponseEntity.ok().build();
    }

    private ChatMessageDto convertToDto(ChatMessage entity) {


        return null;

    }

    private ChatHistory convertChatMessageToDto(ChatMessage entity) {
       return ChatHistory.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .content(entity.getContent())
                .roomId(entity.getRoomId())
                .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}
