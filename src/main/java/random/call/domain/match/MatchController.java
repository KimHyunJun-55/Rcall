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
import random.call.domain.member.Member;
import random.call.domain.member.dto.MemberResponseDTO;
import random.call.domain.member.repository.MemberRepository;
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
    private final MatchService matchService;
    private final MemberRepository memberRepository;

    // 대기열 저장 (실제 운영시 Redis 사용 권장)
    private static final Queue<WaitingUser> waitingQueue = new ConcurrentLinkedQueue<>();

    @MessageMapping("/matching/request/voice")
    public void handleMatchingRequest(Message<?> incomingMessage) throws InterruptedException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);
        String token = jwtUtil.extractToken(accessor);
        Long userId = jwtUtil.getMemberIdToToken(token);
        String nickname = jwtUtil.getNicknameToToken(token);
        Member member = memberRepository.findById(userId).orElse(null);
        Member member2 = memberRepository.findById(2L).orElse(null);
        System.out.println(userId);
        System.out.println(nickname);
//        sendMatchingSuccessMessage(member,member2,2L, MatchType.CALL);

        matchService.processMatching(userId, MatchType.CALL);


        // 1. 대기열에 추가
//        WaitingUser newUser = new WaitingUser(userId, nickname);
//        waitingQueue.add(newUser);
//        log.info("대기열 추가: {}", newUser);
//
//        // 2. 매칭 시도 (최소 2명 이상일 때)
//        if (waitingQueue.size() >= 2) {
//            WaitingUser user1 = waitingQueue.poll();
//            WaitingUser user2 = waitingQueue.poll();
//
//            // 3. 채널 생성 (테스트용 고정값)
//            String channelName = "channel_" + System.currentTimeMillis();
//            String agoraToken = "007eJxTYGDlqZ93..."; // 테스트용 토큰
//
//            // 4. 각 사용자에게 매칭 결과 전송
//            sendMatchResult(user1, user2, channelName, agoraToken);
//            sendMatchResult(user2, user1, channelName, agoraToken);
//        }
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

    private void sendMatchingSuccessMessage(Member user1, Member user2,
                                            Long roomId, MatchType matchType) {

        // user1용 메시지 (user2 정보 포함)
        Map<String, Object> payloadForUser1 = Map.of(
                "roomName", "test",
                "token","007eJxTYGCZJRuhdzZE0V+96XjAvvTCxvv6DwodJv+xaZJLTjohvlGBwTjFwMjMzNDUMMXY0iTZMtXCwtIsxSgpOdXcPMnAzNRw0ke7jIZARoaHn2axMDJAIIjPwlCSWlzCwAAAujkeiw==",
                "matchUser", new MemberResponseDTO(user1),
                "timestamp", System.currentTimeMillis()
        );

        // user2용 메시지 (user1 정보 포함)
        Map<String, Object> payloadForUser2 = Map.of(
                "roomName", "test",
                "token","007eJxTYGCZJRuhdzZE0V+96XjAvvTCxvv6DwodJv+xaZJLTjohvlGBwTjFwMjMzNDUMMXY0iTZMtXCwtIsxSgpOdXcPMnAzNRw0ke7jIZARoaHn2axMDJAIIjPwlCSWlzCwAAAujkeiw==",
                "matchUser", new MemberResponseDTO(user2),
                "timestamp", System.currentTimeMillis()
        );

        log.info("매칭 알림 전송: {}번 → {}번 (Room {})",
                user1.getId(), user2.getId(), roomId);
        log.info("매칭 알림 전송: {}번 → {}번 (Room {})",
                user2.getId(), user1.getId(), roomId);

        // 각 사용자에게 맞는 메시지 전송
        messagingTemplate.convertAndSend("/queue/matching/" + user1.getId(), payloadForUser1);
        messagingTemplate.convertAndSend("/queue/matching/" + user2.getId(), payloadForUser2);
    }
}
