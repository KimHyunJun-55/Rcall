package random.call.domain.match.service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.call.CallParticipant;
import random.call.domain.call.repository.CallParticipantRepository;
import random.call.domain.call.CallRoom;
import random.call.domain.call.repository.CallRoomRepository;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.repository.ChatParticipantRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.chat.entity.ChatRoom;

import random.call.domain.match.MatchType;
import random.call.domain.match.dto.MatchingResponse;
import random.call.domain.member.Member;
import random.call.domain.member.dto.MemberResponseDTO;
import random.call.domain.member.repository.MemberRepository;
import random.call.global.agora.AgoraTokenService;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatParticipantRepository chatParticipantRepository;
    private final CallRoomRepository callRoomRepository;
    private final CallParticipantRepository callParticipantRepository;
    private final AgoraTokenService agoraTokenService;

    // 관심사별 대기열 (채팅/통화 구분)
    private final Map<String, Set<Long>> chatInterestToUsersMap = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> callInterestToUsersMap = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userToInterestsMap = new ConcurrentHashMap<>();
    private final Map<Long, MatchType> userMatchTypeMap = new ConcurrentHashMap<>();

    // 매칭 스레드 풀
    private final ScheduledExecutorService matchingScheduler = Executors.newScheduledThreadPool(4);



    @Transactional
    public void processMatching(Long memberId, MatchType matchType) throws InterruptedException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (userToInterestsMap.containsKey(memberId)) {
            throw new IllegalStateException("User is already in matching queue");
        }

        // 사용자 관심사 및 매칭 타입 저장
        Set<String> interests = new HashSet<>(member.getInterest());
        userToInterestsMap.put(memberId, interests);
        userMatchTypeMap.put(memberId, matchType);

        // 관심사 매핑 업데이트 (매칭 타입별로 분리)
        Map<String, Set<Long>> targetMap = matchType == MatchType.CHAT ?
                chatInterestToUsersMap : callInterestToUsersMap;

        for (String interest : interests) {
            targetMap.computeIfAbsent(interest, k -> ConcurrentHashMap.newKeySet())
                    .add(memberId);
        }


        broadcastMatchingCount(matchType);

        // 즉시 매칭 시도
        Optional<MatchResult> immediateMatch = tryImmediateMatch(member, interests, matchType);
        if (immediateMatch.isPresent()) {
            MatchResult result = immediateMatch.get();
            Thread.sleep(3_000); // UX를 위한 지연

            sendMatchingSuccess(member, result.matchedMember(),
                    result.roomId(), matchType);
            return;
        }



        // 매칭 실패 시 일정 시간 후 자동 취소
        matchingScheduler.schedule(() -> removeFromMatchingPool(memberId), 600, TimeUnit.SECONDS);
    }


    private Optional<MatchResult> tryImmediateMatch(Member member,
                                                    Set<String> interests, MatchType matchType) {

        Long memberId = member.getId();
        Map<Long, Integer> candidateMatches = new HashMap<>();

        // 매칭 타입에 따라 다른 대기열 사용
        Map<String, Set<Long>> targetMap = matchType == MatchType.CHAT ?
                chatInterestToUsersMap : callInterestToUsersMap;

        // 1. 공통 관심사 가진 사용자 찾기
        for (String interest : interests) {
            Set<Long> usersWithSameInterest = targetMap.get(interest);
            if (usersWithSameInterest != null) {
                for (Long candidateId : usersWithSameInterest) {
                    if (!candidateId.equals(memberId)) {
                        // 후보 사용자의 공통 관심사 수 카운트
                        candidateMatches.merge(candidateId, 1, Integer::sum);
                    }
                }
            }
        }

        // 2. 최소 3개 이상 공통 관심사 있는 사용자 선택
        Optional<Long> matchedUserId = candidateMatches.entrySet().stream()
                .filter(entry -> entry.getValue() >= 1)
                .map(Map.Entry::getKey)
                .findFirst();

        if (matchedUserId.isPresent()) {
            Long matchedId = matchedUserId.get();
            Member matchedMember = getMember(matchedId);

            // 3. 채팅방/통화방 생성
            Long roomId = createRoom(member, matchedMember, matchType);

            // 4. 대기열에서 제거
            removeFromMatchingPool(memberId);
            removeFromMatchingPool(matchedId);

            return Optional.of(new MatchResult(roomId, matchedMember));
        }

        return Optional.empty();
    }


    private void sendMatchingSuccess(Member user1, Member user2, Long roomId, MatchType matchType) {
        MatchingResponse response1 = createResponse(user1, user2, roomId, matchType);
        MatchingResponse response2 = createResponse(user2, user1, roomId, matchType);

        messagingTemplate.convertAndSend("/queue/matching/" + user1.getId(), response1);
        messagingTemplate.convertAndSend("/queue/matching/" + user2.getId(), response2);
    }

    private MatchingResponse createResponse(Member current, Member partner, Long roomId, MatchType matchType) {
        if (matchType == MatchType.CALL) {
            String token = agoraTokenService.generateToken(roomId.toString(), current.getId().intValue());
            return new MatchingResponse(
                    matchType,
                    roomId,
                    new MemberResponseDTO(partner),
                    token
            );
        } else {
            return new MatchingResponse(
                    matchType,
                    roomId,
                    Map.of(
                            "id", partner.getId(),
                            "nickname", partner.getNickname()
                    ),
                    null
            );
        }
    }


    @Transactional
    public Long createRoom(Member member1, Member member2, MatchType matchType) {
        if (matchType == MatchType.CHAT) {
            return createChatRoom(member1, member2);
        } else {
            return createCallRoom(member1, member2);
        }
    }

    private Long createChatRoom(Member member1, Member member2) {
        String roomId = "chat-" + UUID.randomUUID();
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

        chatRoomRepository.save(room);
        chatParticipantRepository.saveAll(List.of(participant1, participant2));
        return room.getId();
    }

    private Long createCallRoom(Member member1, Member member2) {
        String roomId = "call-" + UUID.randomUUID();

        CallRoom room = CallRoom.builder()
                .roomId(roomId)
                .build();

        CallParticipant participant1 = CallParticipant.builder()
                .callRoom(room)
                .member(member1)
                .build();

        CallParticipant participant2 = CallParticipant.builder()
                .callRoom(room)
                .member(member2)
                .build();

        callRoomRepository.save(room);
        callParticipantRepository.saveAll(List.of(participant1,participant2));
        return room.getId();
    }

    
    //매칭취소 및 삭제
    public void removeFromMatchingPool(Long memberId) {
        Set<String> interests = userToInterestsMap.remove(memberId);
        MatchType matchType = userMatchTypeMap.remove(memberId);

        if (interests != null && matchType != null) {
            Map<String, Set<Long>> targetMap = matchType == MatchType.CHAT ?
                    chatInterestToUsersMap : callInterestToUsersMap;

            for (String interest : interests) {
                Set<Long> users = targetMap.get(interest);
                if (users != null) {
                    users.remove(memberId);
                    if (users.isEmpty()) {
                        targetMap.remove(interest);
                    }
                }
            }
        }
        System.out.println("[ "+memberId+" ]매칭취소요청");
        broadcastMatchingCount(matchType);
    }

    private void broadcastMatchingCount(MatchType matchType) {
        int count = getCount(matchType);
        String topic = matchType == MatchType.CHAT ?
                "/topic/matching-count-chat" : "/topic/matching-count-call";
        messagingTemplate.convertAndSend(topic, count);
    }

    public int getCount(MatchType matchType) {
        return (int) userMatchTypeMap.values().stream()
                .filter(type -> type == matchType)
                .count();
    }
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
    }

    private record MatchResult(Long roomId, Member matchedMember) {}

    private int convertUidSafely(Long uid) {
        if (uid > Integer.MAX_VALUE || uid < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("UID가 int 범위를 초과합니다.");
        }
        return uid.intValue();
    }

}