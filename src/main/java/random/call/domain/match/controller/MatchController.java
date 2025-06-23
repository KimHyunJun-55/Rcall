package random.call.domain.match.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.match.MatchType;
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.MatchRequestTest;
import random.call.domain.match.service.MatchService;
import random.call.domain.member.repository.MemberRepository;
import random.call.global.jwt.JwtUtil;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MatchController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    private final MatchService matchService;
    private final MemberRepository memberRepository;

    // 대기열 저장 (실제 운영시 Redis 사용 권장)
    private static final Queue<WaitingUser> waitingQueue = new ConcurrentLinkedQueue<>();

    @MessageMapping("/matching/request/voice")
    public void sendMessage(@Payload MatchRequest dto) {
        matchService.processMatching(dto, MatchType.CALL);
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

//    private void sendMatchResult(WaitingUser receiver, WaitingUser partner,
//                                 String channelName, String agoraToken) {
//        MatchingResponse response = MatchingResponse.builder()
//                .roomId(1L)
//                .agoraToken(agoraToken)
//                .channelName(channelName)
//                .matchedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
//                .matchMember(partner.getNickname())
//                .matchMemberId(partner.getUserId())
//                .build();
//
//        messagingTemplate.convertAndSend(
//                "/queue/matching/voice/" + receiver.getUserId(),
//                response
//        );
//    }

    @Getter
    @AllArgsConstructor
    private static class WaitingUser {
        private Long userId;
        private String nickname;
    }

//    private void sendMatchingSuccessMessage(Member user1, Member user2,
//                                            Long roomId, MatchType matchType) {
//
//        // user1용 메시지 (user2 정보 포함)
//        Map<String, Object> payloadForUser1 = Map.of(
//                "roomName", "test",
//                "token","007eJxTYGCZJRuhdzZE0V+96XjAvvTCxvv6DwodJv+xaZJLTjohvlGBwTjFwMjMzNDUMMXY0iTZMtXCwtIsxSgpOdXcPMnAzNRw0ke7jIZARoaHn2axMDJAIIjPwlCSWlzCwAAAujkeiw==",
//                "matchUser", new MemberResponseDTO(user1),
//                "timestamp", System.currentTimeMillis()
//        );
//
//        // user2용 메시지 (user1 정보 포함)
//        Map<String, Object> payloadForUser2 = Map.of(
//                "roomName", "test",
//                "token","007eJxTYGCZJRuhdzZE0V+96XjAvvTCxvv6DwodJv+xaZJLTjohvlGBwTjFwMjMzNDUMMXY0iTZMtXCwtIsxSgpOdXcPMnAzNRw0ke7jIZARoaHn2axMDJAIIjPwlCSWlzCwAAAujkeiw==",
//                "matchUser", new MemberResponseDTO(user2),
//                "timestamp", System.currentTimeMillis()
//        );
//
//        log.info("매칭 알림 전송: {}번 → {}번 (Room {})",
//                user1.getId(), user2.getId(), roomId);
//        log.info("매칭 알림 전송: {}번 → {}번 (Room {})",
//                user2.getId(), user1.getId(), roomId);
//
//        // 각 사용자에게 맞는 메시지 전송
//        messagingTemplate.convertAndSend("/queue/matching/" + user1.getId(), payloadForUser1);
//        messagingTemplate.convertAndSend("/queue/matching/" + user2.getId(), payloadForUser2);
//    }
}
