package random.call.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.MatchingResponse;
import random.call.domain.match.dto.VoiceMatchResponse;
import random.call.domain.match.service.MatchService;
import random.call.global.jwt.JwtUtil;
import random.call.global.security.userDetails.JwtUserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
@Controller
public class MatchController {

    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;

    // 대기열 저장 (실제 운영시 Redis 사용 권장)
    private static final Queue<WaitingUser> waitingQueue = new ConcurrentLinkedQueue<>();

    @MessageMapping("/matching/voice/request")
    public void handleMatchingRequest(Message<?> incomingMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);
        String token = jwtUtil.extractToken(accessor);
        Long userId = jwtUtil.getMemberIdToToken(token);
        String nickname = jwtUtil.getNicknameToToken(token);

        // 1. 대기열에 추가
        WaitingUser newUser = new WaitingUser(userId, nickname);
        waitingQueue.add(newUser);
        log.info("대기열 추가: {}", newUser);

        // 2. 매칭 시도 (최소 2명 이상일 때)
        if (waitingQueue.size() >= 2) {
            WaitingUser user1 = waitingQueue.poll();
            WaitingUser user2 = waitingQueue.poll();

            // 3. 채널 생성 (테스트용 고정값)
            String channelName = "channel_" + System.currentTimeMillis();
            String agoraToken = "007eJxTYGDlqZ93..."; // 테스트용 토큰

            // 4. 각 사용자에게 매칭 결과 전송
            sendMatchResult(user1, user2, channelName, agoraToken);
            sendMatchResult(user2, user1, channelName, agoraToken);
        }
    }

    private void sendMatchResult(WaitingUser receiver, WaitingUser partner,
                                 String channelName, String agoraToken) {
        MatchingResponse response = MatchingResponse.builder()
                .roomId(1L)
                .agoraToken(agoraToken)
                .channelName(channelName)
                .matchedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .matchMember(partner.getNickname())
                .matchMemberId(partner.getUserId())
                .build();

        messagingTemplate.convertAndSend(
                "/queue/matching/voice/" + receiver.getUserId(),
                response
        );
    }

    @Getter
    @AllArgsConstructor
    private static class WaitingUser {
        private Long userId;
        private String nickname;
    }
}
