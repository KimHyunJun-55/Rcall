package random.call.global.webSocket;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import random.call.domain.chat.repository.ChatParticipantRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.match.service.ChatMatchService;
import random.call.domain.member.Member;
import random.call.domain.member.MemberRepository;
import random.call.global.jwt.JwtUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final ChatMatchService matchService;
    private final SimpMessagingTemplate messagingTemplate; // 추가


    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;


    // Raw WebSocket 테스트
    @GetMapping("/ws-test")
    public String rawWebSocketTest() {
        return "연결 확인 페이지";
    }

    // STOMP 메시지 처리
    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public String handleChat(String message, Message<?> incomingMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);
        String username = (String) accessor.getSessionAttributes().get("user");  // 세션에서 사용자 이름 꺼내기

        System.out.println("인증된 사용자: " + username);

        // 메시지와 사용자 정보 함께 반환
        return "서버 응답: " + message + " (수신 시간: " + new Date() + ") 사용자: " + username;
    }



    @MessageMapping("/matching/request")
    public void handleMatchingRequest(Message<?> incomingMessage) throws InterruptedException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);
        Long memberId = (Long) accessor.getSessionAttributes().get("id");

        if (memberId == null) {
            throw new IllegalStateException("세션에 사용자 ID가 없습니다.");
        }

        String token = jwtUtil.extractToken(accessor);
        var userId = jwtUtil.getMemberIdToToken(token);

        // 🕐 매칭 지연 (테스트용)
        Thread.sleep(15_000); // 지연
//        matchService.processMatching(memberId);

        Member member = memberRepository.findById(userId).orElseThrow(()->new EntityNotFoundException("회원없음"));
        Member member2 = memberRepository.findById(2L).orElseThrow(()->new EntityNotFoundException("회원없음"));

//        String roomId = "room-" + UUID.randomUUID();
        String roomName = "room-123";
        Long roomId =extracted(roomName, member, member2);

        messagingTemplate.convertAndSend(
                "/queue/matching/" + userId,
                Map.of(
                        "roomId", 1L,
                        "matchedUser", 2L
                )
        );
    }

    @Transactional
    public Long extracted(String roomId, Member member, Member member2) {
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .build();
        ChatParticipant participant1 = ChatParticipant.builder()
                .chatRoom(room)
                .member(member)
                .joinedAt(LocalDateTime.now())
                .build();

        ChatParticipant participant2 = ChatParticipant.builder()
                .chatRoom(room)
                .member(member2)
                .joinedAt(LocalDateTime.now())
                .build();


//        room.setParticipants(List.of(participant1, participant2));
        chatRoomRepository.save(room).getRoomId();
        chatParticipantRepository.saveAll(List.of(participant1, participant2)); // 명시적 저장

        return room.getId();
    }







}
