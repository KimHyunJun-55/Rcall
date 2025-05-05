package random.call.domain.match.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.member.Member;
import random.call.domain.member.MemberRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class ChatMatchService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 관심사별 대기열 (스레드 안전한 구조)
    private final Map<String, Set<Long>> interestToUsersMap = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userToInterestsMap = new ConcurrentHashMap<>();

    // 매칭 스레드 풀
    private final ScheduledExecutorService matchingScheduler = Executors.newScheduledThreadPool(4);

    @Transactional
    public void processMatching(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (userToInterestsMap.containsKey(memberId)) {
            throw new IllegalStateException("User is already in matching queue");
        }

        // 사용자 관심사 정보 저장
        Set<String> interests = new HashSet<>(member.getInterest());
        userToInterestsMap.put(memberId, interests);

        // 관심사 매핑 업데이트
        for (String interest : interests) {
            interestToUsersMap.computeIfAbsent(interest, k -> ConcurrentHashMap.newKeySet())
                    .add(memberId);
        }

        // 1. 즉시 매칭 시도
        Optional<MatchResult> immediateMatch = tryImmediateMatch(memberId, interests);
        if (immediateMatch.isPresent()) {
            MatchResult result = immediateMatch.get();

            // 매칭된 양쪽 사용자에게 모두 메시지 전송
            sendMatchingSuccessMessage(memberId, result.matchedUserId(), result.roomId());
            return;
        }

        // 2. 매칭 실패 시 30초 후 자동 취소
        matchingScheduler.schedule(() -> removeFromMatchingPool(memberId), 30, TimeUnit.SECONDS);
    }

    private void sendMatchingSuccessMessage(Long user1, Long user2, String roomId) {
        // 첫 번째 사용자에게 메시지 전송
        messagingTemplate.convertAndSend(
                "/queue/matching/" + user1,
                Map.of(
                        "roomId", roomId,
                        "matchedUser", user2
                )
        );

        // 두 번째 사용자에게 메시지 전송
        messagingTemplate.convertAndSend(
                "/queue/matching/" + user2,
                Map.of(
                        "roomId", roomId,
                        "matchedUser",user1
                )
        );
    }

    private Optional<MatchResult> tryImmediateMatch(Long memberId, Set<String> interests) {
        Map<Long, Integer> candidateMatches = new HashMap<>();

        for (String interest : interests) {
            Set<Long> usersWithSameInterest = interestToUsersMap.get(interest);
            if (usersWithSameInterest != null) {
                for (Long candidateId : usersWithSameInterest) {
                    if (!candidateId.equals(memberId)) {
                        candidateMatches.merge(candidateId, 1, Integer::sum);
                    }
                }
            }
        }

        Optional<Long> matchedUserId = candidateMatches.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .findFirst();

        if (matchedUserId.isPresent()) {
            Long matchedId = matchedUserId.get();
            String roomId = createChatRoom(memberId, matchedId);
            removeFromMatchingPool(memberId);
            removeFromMatchingPool(matchedId);
            return Optional.of(new MatchResult(roomId, matchedId));
        }

        return Optional.empty();
    }

    private void removeFromMatchingPool(Long memberId) {
        Set<String> interests = userToInterestsMap.remove(memberId);
        if (interests != null) {
            for (String interest : interests) {
                Set<Long> users = interestToUsersMap.get(interest);
                if (users != null) {
                    users.remove(memberId);
                    if (users.isEmpty()) {
                        interestToUsersMap.remove(interest);
                    }
                }
            }
        }
    }

    @Transactional
    private String createChatRoom(Long user1, Long user2) {
        Member member1 = getMember(user1);
        Member member2 = getMember(user2);
        String roomId = "room-" + UUID.randomUUID();

        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .build();

        ChatParticipant participant1 = ChatParticipant.builder()
                .chatRoom(room)
                .member(member1)
                .joinedAt(LocalDateTime.now())
                .build();

        ChatParticipant participant2 = ChatParticipant.builder()
                .chatRoom(room)
                .member(member2)
                .joinedAt(LocalDateTime.now())
                .build();

//        room.setParticipants(List.of(participant1, participant2));

        return chatRoomRepository.save(room).getRoomId();
    }

    private Member getMember (Long memberId){
        return memberRepository.findById(memberId).orElseThrow(()-> new EntityNotFoundException("해당 유저가 없습니다."));

    }

    private record MatchResult(String roomId, Long matchedUserId) {}
}