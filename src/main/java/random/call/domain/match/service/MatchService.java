package random.call.domain.match.service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.match.MatchType;
import random.call.domain.match.WaitingUser;
import random.call.domain.match.dto.ChatMatchResponse;
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.VoiceMatchResponse;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.sessions.Session;
import random.call.domain.sessions.SessionService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final SessionService sessionService;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 카테고리별로 Talker 대기 큐
    private final Map<String, Queue<Long>> talkerQueues = new ConcurrentHashMap<>();

    // 카테고리별로 Listener 대기 큐
    private final Map<String, Queue<Long>> listenerQueues = new ConcurrentHashMap<>();

    // ALL 카테고리 Listener 전용 큐
    private final Queue<Long> allListenerQueue = new ConcurrentLinkedQueue<>();

    // 채팅 대기 중인 우선순위 큐
    private final Map<String, PriorityQueue<WaitingUser>> chatWaitingQueue = new ConcurrentHashMap<>();

    public ChatMatchResponse chatMatch(Long memberId, MatchType matchType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        List<String> myInterests = member.getInterest(); // 예: ["game", "music", "travel"]
        Long bestMatchId = null;
        int maxScore = 0;

        // 매칭을 위한 점수 계산 후 우선순위 큐에서 가장 높은 점수의 유저를 찾음
        for (Map.Entry<String, PriorityQueue<WaitingUser>> entry : chatWaitingQueue.entrySet()) {
            String interest = entry.getKey();
            PriorityQueue<WaitingUser> queue = entry.getValue();

            for (WaitingUser waitingUser : queue) {
                if (waitingUser.getUserId().equals(memberId)) continue;

                Member waitingMember = memberRepository.findById(waitingUser.getUserId())
                        .orElse(null);
                if (waitingMember == null) continue;

                List<String> waitingInterests = waitingMember.getInterest();
                int score = calculateMatchScore(myInterests, waitingInterests);

                // 매칭 점수 업데이트
//                if (score > maxScore) {
//                    maxScore = score;
//                    bestMatchId = waitingUser.getUserId();
//                }
//            if (maxScore > 3) break;  // 점수가 3점 이상이면 바로 매칭
                if (score >= 3) {
                    bestMatchId = waitingUser.getUserId();
                    break; // 바로 매칭 종료
                }
            }

        }

        if (bestMatchId != null && maxScore > 0) {
            // 매칭된 상대를 큐에서 제거하고 채팅방 생성
            final Long finalBestMatchId = bestMatchId;
            chatWaitingQueue.values().forEach(queue -> queue.removeIf(user -> user.getUserId().equals(finalBestMatchId)));

            // 룸 생성
            String roomId = UUID.randomUUID().toString();
            ChatRoom room = ChatRoom.builder()
                    .roomId(roomId)
//                    .participants(List.of(memberId.toString(), bestMatchId.toString()))
                    .build();
            chatRoomRepository.save(room);

            return new ChatMatchResponse(roomId, true);
        }

        // 매칭 실패 시 대기 큐에 추가
        chatWaitingQueue
                .computeIfAbsent(member.getInterest().get(0), k -> new PriorityQueue<>())
                .offer(new WaitingUser(memberId));

        return new ChatMatchResponse(null, false);
    }

    private int calculateMatchScore(List<String> a, List<String> b) {
        Set<String> aSet = new HashSet<>(a);
        Set<String> bSet = new HashSet<>(b);
        aSet.retainAll(bSet);
        return aSet.size(); // 겹치는 관심사 수
    }







    public VoiceMatchResponse requestAsTalker(Long userId, String category) {
        // 1. 해당 카테고리의 Listener 있는지 먼저 탐색
        Queue<Long> specificListeners = listenerQueues.getOrDefault(category, new ConcurrentLinkedQueue<>());
        Long matchedListener = specificListeners.poll();

        if (matchedListener == null) {
            matchedListener = allListenerQueue.poll();
        }

        if (matchedListener != null) {
            Session session = sessionService.createSession(userId, matchedListener);
//            return new VoiceMatchResponse(session.getChannelName(), session.getAgoraToken(), session.getAgoraToken());
            return null;
        }

        // 없으면 대기열에 본인 추가
        talkerQueues.computeIfAbsent(category, k -> new ConcurrentLinkedQueue<>()).offer(userId);
        return new VoiceMatchResponse(null, null, "WAITING");
    }

    public VoiceMatchResponse requestAsListener(Long userId, String category) {
        // ALL 리스너
        if ("ALL".equalsIgnoreCase(category)) {
            // 대기 중인 모든 Talker 탐색
            for (Map.Entry<String, Queue<Long>> entry : talkerQueues.entrySet()) {
                Long matchedTalker = entry.getValue().poll();
                if (matchedTalker != null) {
                    Session session = sessionService.createSession(matchedTalker, userId);
//                    return new VoiceMatchResponse(session.getChannelName(), session.getAgoraToken(), session.getAgoraToken());
                    return null;
                }
            }

            // 없으면 ALL 큐에 대기
            allListenerQueue.offer(userId);
            return new VoiceMatchResponse(null, null, "WAITING");
        }

        // 카테고리별 Talker 큐 확인
        Long matchedTalker = talkerQueues.getOrDefault(category, new ConcurrentLinkedQueue<>()).poll();
        if (matchedTalker != null) {
            Session session = sessionService.createSession(matchedTalker, userId);
//            return new VoiceMatchResponse(session.getChannelName(), session.getAgoraToken(), session.getAgoraToken());
            return null;
        }

        // 없으면 해당 카테고리 Listener 큐에 대기
        listenerQueues.computeIfAbsent(category, k -> new ConcurrentLinkedQueue<>()).offer(userId);
        return new VoiceMatchResponse(null, null, "WAITING");
    }

    public VoiceMatchResponse processMatchRequest(Long id, MatchRequest request) {
        return null;
    }
}
