package random.call.domain.match.service;
import jakarta.annotation.PostConstruct;
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
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.MatchingResponse;
import random.call.domain.member.Member;
import random.call.domain.member.dto.MemberResponseDTO;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.type.Gender;
import random.call.global.agora.AgoraTokenService;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 완전한 기능을 갖춘 매칭 서비스 구현체
 * - 실시간 채팅/통화 매칭 처리
 * - 관심사 기반 채팅 매칭
 * - 조건 기반 통화 매칭
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
    // ========== 의존성 주입 ========== //
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatParticipantRepository chatParticipantRepository;
    private final CallRoomRepository callRoomRepository;
    private final CallParticipantRepository callParticipantRepository;
    private final AgoraTokenService agoraTokenService;

    // ========== 매칭 풀 자료구조 ========== //
    private final Map<String, Set<Long>> chatInterestToUsersMap = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> callCategoryToUsersMap = new ConcurrentHashMap<>();
    private final Map<Long, CallMatchContext> callUserContexts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> callCategoryCounts = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userToInterestsMap = new ConcurrentHashMap<>();
    private final Map<Long, MatchType> userMatchTypeMap = new ConcurrentHashMap<>();

    // ========== 카운터 & 스케줄러 ========== //
    private final AtomicInteger chatMatchCounter = new AtomicInteger();
    private final AtomicInteger callMatchCounter = new AtomicInteger();
    private final ScheduledExecutorService matchingScheduler = Executors.newScheduledThreadPool(4);
    private final ReentrantLock poolLock = new ReentrantLock();

    // ========== 상수 정의 ========== //
    private static final long CHAT_TIMEOUT_MINUTES = 10;
    private static final long CALL_TIMEOUT_MINUTES = 7;
    private static final long MAX_PROCESSING_TIME_MS = 100;
    private static final int MAX_BROADCAST_RETRY = 2;

    /**
     * 통화 매칭 조건 컨텍스트
     */
    private static class CallMatchContext {
        final int currentAge;
        final int minAge;
        final int maxAge;
        final Gender selectedGender;
        final Gender realGender;
        final String category;

        CallMatchContext(int currentAge, int minAge, int maxAge,
                         Gender selectedGender, Gender realGender, String category) {
            validateAgeRange(currentAge, minAge, maxAge);
            this.currentAge = currentAge;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.realGender = realGender;
            this.selectedGender = selectedGender;
            this.category = category;

            log.debug("새 CallMatchContext 생성 - 사용자 나이: {}, 원하는 나이범위: {}-{}, 원하는 성별: {}, 실제 성별: {}, 카테고리: {}",
                    currentAge, minAge, maxAge, selectedGender, realGender, category);
        }

        private void validateAgeRange(int currentAge, int minAge, int maxAge) {
            if (currentAge < minAge || currentAge > maxAge) {
                log.error("나이 범위 검증 실패 - 사용자 나이: {}, 허용 범위: {}-{}", currentAge, minAge, maxAge);
                throw new MatchingException("나이 범위 검증 실패");
            }
        }
    }

    // ========== 공개 메서드 ========== //

    /**
     * 매칭 요청 처리
     */
    @Transactional
    public void processMatching(MatchRequest request, MatchType matchType) {
        long startTime = System.nanoTime();
        log.info("매칭 요청 시작 - 사용자 ID: {}, 타입: {}, 나이범위: {}-{}, 성별: {}, 카테고리: {}",
                request.getUserId(), matchType, request.getMinAge(), request.getMaxAge(),
                request.getGender(), request.getCategory());

        try {
            Member member = getMemberOrThrow(request.getUserId());
            log.debug("사용자 정보 조회 완료 - ID: {}, 닉네임: {}, 나이: {}, 성별: {}",
                    member.getId(), member.getNickname(), member.getAge(), member.getGender());

            validateDuplicateRequest(member.getId());
            log.debug("중복 요청 검증 완료");

            registerToMatchingPool(member, request, matchType);
            log.debug("매칭 풀 등록 완료");

            tryImmediateMatching(member, matchType);

            scheduleMatchingTimeout(member.getId(), matchType);
            log.debug("매칭 타임아웃 스케줄 등록 완료");

        } catch (Exception e) {
            handleMatchingError(request.getUserId(), matchType, e);
            log.error("매칭 처리 중 오류 발생 - 사용자 ID: {}, 오류: {}", request.getUserId(), e.getMessage(), e);
            throw e;
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            log.debug("매칭 요청 처리 완료 - 소요시간: {}ms", durationMs);
            if (durationMs > MAX_PROCESSING_TIME_MS) {
                log.warn("매칭 처리 지연 - 소요시간: {}ms", durationMs);
            }
        }
    }

    /**
     * 매칭 풀에서 사용자 제거
     */
    public void removeFromMatchingPool(Long memberId) {
        poolLock.lock();
        log.debug("매칭 풀 제거 시작 - 사용자 ID: {}", memberId);

        try {
            MatchType matchType = userMatchTypeMap.remove(memberId);
            if (matchType == null) {
                log.info("매칭 풀에 없는 사용자 - ID: {}", memberId);
                return;
            }

            try {
                if (matchType == MatchType.CHAT) {
                    log.debug("채팅 매칭 풀에서 제거 - 사용자 ID: {}", memberId);
                    removeFromChatPool(memberId);
                } else {
                    log.debug("통화 매칭 풀에서 제거 - 사용자 ID: {}", memberId);
                    removeFromCallPool(memberId);
                }

                userToInterestsMap.remove(memberId);
                log.info("사용자 매칭 풀 제거 완료 - ID: {}, 타입: {}", memberId, matchType);

                safeBroadcast(matchType, MAX_BROADCAST_RETRY);
            } catch (Exception e) {
                userMatchTypeMap.put(memberId, matchType);
                log.error("매칭 풀 제거 실패 - 사용자 ID: {}, 타입: {}", memberId, matchType, e);
                throw new MatchingException("매칭 풀 제거 실패", e);
            }
        } finally {
            poolLock.unlock();
        }
    }

    // ========== 비즈니스 로직 ========== //

    private void registerToMatchingPool(Member member, MatchRequest request, MatchType matchType) {
        log.debug("매칭 풀 등록 시작 - 사용자 ID: {}, 타입: {}", member.getId(), matchType);

        Set<String> interests = new HashSet<>(member.getInterest());
        userToInterestsMap.put(member.getId(), interests);
        userMatchTypeMap.put(member.getId(), matchType);

        if (matchType == MatchType.CHAT) {
            log.debug("채팅 매칭 풀 등록 - 사용자 ID: {}, 관심사: {}", member.getId(), interests);
            processChatMatching(member.getId(), interests);
            chatMatchCounter.incrementAndGet();
        } else {
            log.debug("통화 매칭 풀 등록 - 사용자 ID: {}", member.getId());
            processCallMatching(member, request);
            callMatchCounter.incrementAndGet();
        }
    }
    private void processChatMatching(Long memberId, Set<String> interests) {
        poolLock.lock();
        try {
            interests.forEach(interest ->
                    chatInterestToUsersMap
                            .computeIfAbsent(interest, k -> ConcurrentHashMap.newKeySet())
                            .add(memberId)
            );
            chatMatchCounter.set(chatInterestToUsersMap.values().stream().mapToInt(Set::size).sum() / 2); // 중복 제거
        } finally {
            poolLock.unlock();
            broadcastMatchingCount(MatchType.CHAT);
        }
    }

    private void processCallMatching(Member member, MatchRequest request) {
        log.debug("통화 매칭 처리 시작 - 사용자 ID: {}, 나이: {}, 성별: {}",
                member.getId(), member.getAge(), member.getGender());

        CallMatchContext context = new CallMatchContext(
                member.getAge(),
                request.getMinAge(),
                request.getMaxAge(),
                request.getGender(),
                member.getGender(),
                request.getCategory()
        );

        callUserContexts.put(member.getId(), context);
        callCategoryToUsersMap
                .computeIfAbsent(request.getCategory(), k -> ConcurrentHashMap.newKeySet())
                .add(member.getId());

        updateCategoryCount(request.getCategory(), 1);
        log.debug("통화 매칭 풀 등록 완료 - 사용자 ID: {}, 카테고리: {}", member.getId(), request.getCategory());
    }

    private void tryImmediateMatching(Member member, MatchType matchType) {
        tryImmediateMatch(member, matchType).ifPresent(result ->
                sendMatchingSuccess(member, result.matchedMember(), result.roomId(), matchType)
        );
    }

    private Optional<MatchResult> tryImmediateMatch(Member member, MatchType matchType) {
        return matchType == MatchType.CHAT ?
                tryChatMatch(member) :
                tryCallMatch(member);
    }

    private Optional<MatchResult> tryChatMatch(Member member) {
        return userToInterestsMap.get(member.getId()).stream()
                .flatMap(interest -> chatInterestToUsersMap.getOrDefault(interest, Collections.emptySet()).stream())
                .filter(id -> !id.equals(member.getId()))
                .findFirst()
                .map(id -> createMatchResult(member, id, MatchType.CHAT));
    }
//private Optional<MatchResult> tryChatMatch(Member member) {
//    // 무조건 5번 유저와 매칭 (매칭 풀 확인 없이)
//    Long fixedMatchedUserId = 4L;
//
//    try {
//        // 5번 유저 정보 조회 (없으면 예외 발생)
//        Member matchedUser = getMemberOrThrow(fixedMatchedUserId);
//        return Optional.of(createMatchResult(member, fixedMatchedUserId, MatchType.CHAT));
//    } catch (EntityNotFoundException e) {
//        log.error("5번 유저를 찾을 수 없습니다", e);
//        return Optional.empty();
//    }
//}

    private Optional<MatchResult> tryCallMatch(Member member) {
        log.debug("통화 즉시 매칭 시도 - 사용자 ID: {}", member.getId());

        CallMatchContext requester = callUserContexts.get(member.getId());
        if (requester == null) {
            log.warn("통화 매칭 컨텍스트 없음 - 사용자 ID: {}", member.getId());
            return Optional.empty();
        }

        log.debug("통화 매칭 조건 - 나이: {}-{}, 성별: {}, 카테고리: {}",
                requester.minAge, requester.maxAge, requester.selectedGender, requester.category);

        return callCategoryToUsersMap.getOrDefault(requester.category, Collections.emptySet()).stream()
                .filter(id -> !id.equals(member.getId()))
                .peek(id -> log.debug("매칭 후보 사용자 ID: {}", id))
                .map(id -> {
                    CallMatchContext candidate = callUserContexts.get(id);
                    double score = calculateMatchScore(requester, candidate);
                    log.debug("매칭 점수 - 사용자 {} vs {}: {}", member.getId(), id, score);
                    return new AbstractMap.SimpleEntry<>(id, score);
                })
                .filter(entry -> {
                    boolean passed = entry.getValue() > 0.5;
                    if (!passed) {
                        log.debug("매칭 점수 부족으로 필터링 - 사용자: {}, 점수: {}", entry.getKey(), entry.getValue());
                    }
                    return passed;
                })
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(id -> {
                    log.info("통화 매칭 성공 - 사용자 {}와 {} 매칭", member.getId(), id);
                    return createMatchResult(member, id, MatchType.CALL);
                });
    }

    private double calculateMatchScore(CallMatchContext requester, CallMatchContext candidate) {
        if (candidate == null) {
            log.debug("매칭 점수 계산 - 후보가 null");
            return 0;
        }

        boolean isMutual = isMutualMatch(requester, candidate);
        if (!isMutual) {
            log.debug("상호 매칭 조건 불일치 - 요청자: {}-{}세/{}, 후보: {}-{}세/{}",
                    requester.minAge, requester.maxAge, requester.selectedGender,
                    candidate.minAge, candidate.maxAge, candidate.selectedGender);
            return 0;
        }

        double ageScore = 1.0 - (Math.abs(requester.currentAge - candidate.currentAge) / 15.0);
        double genderScore = requester.selectedGender == Gender.ANY ? 0.9 : 1.0;
        double totalScore = (ageScore * 0.6 + genderScore * 0.4);

        log.debug("매칭 점수 계산 - 나이점수: {}, 성별점수: {}, 총점: {}", ageScore, genderScore, totalScore);
        return totalScore;
    }

    private boolean isMutualMatch(CallMatchContext requester, CallMatchContext candidate) {
        if (candidate == null) {
            log.debug("상호 매칭 검사 - 후보가 null");
            return false;
        }

        boolean ageMatch = (requester.minAge <= candidate.currentAge && candidate.currentAge <= requester.maxAge) &&
                (candidate.minAge <= requester.currentAge && requester.currentAge <= candidate.maxAge);

        boolean genderMatch = requester.selectedGender == Gender.ANY ||
                candidate.realGender == requester.selectedGender;

        log.debug("상호 매칭 검사 - 나이일치: {}, 성별일치: {}", ageMatch, genderMatch);
        return ageMatch && genderMatch;
    }

    // ========== 방 생성 로직 ========== //

    @Transactional
    public Long createRoom(Member member1, Member member2, MatchType matchType) {
        return matchType == MatchType.CHAT ?
                createChatRoom(member1, member2) :
                createCallRoom(member1, member2);
    }

    private Long createChatRoom(Member member1, Member member2) {
        String roomId = "chat-" + UUID.randomUUID();
        ChatRoom room = ChatRoom.builder().roomId(roomId).build();

        ChatParticipant participant1 = buildChatParticipant(room, member1);
        ChatParticipant participant2 = buildChatParticipant(room, member2);

        chatRoomRepository.save(room);
        chatParticipantRepository.saveAll(List.of(participant1, participant2));
        return room.getId();
    }

    private ChatParticipant buildChatParticipant(ChatRoom room, Member member) {
        return ChatParticipant.builder()
                .chatRoom(room)
                .member(member)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    private Long createCallRoom(Member member1, Member member2) {
        String roomId = "call-" + UUID.randomUUID();
        CallRoom room = CallRoom.builder().roomId(roomId).build();

        CallParticipant participant1 = buildCallParticipant(room, member1);
        CallParticipant participant2 = buildCallParticipant(room, member2);

        callRoomRepository.save(room);
        callParticipantRepository.saveAll(List.of(participant1, participant2));
        return room.getId();
    }

    private CallParticipant buildCallParticipant(CallRoom room, Member member) {
        return CallParticipant.builder()
                .callRoom(room)
                .member(member)
                .build();
    }

    // ========== 유틸리티 메서드 ========== //

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 없음: " + memberId));
    }

    private void validateDuplicateRequest(Long memberId) {
        poolLock.lock();
        try {
            if (userMatchTypeMap.containsKey(memberId)) {
                throw new MatchingException("이미 매칭 대기중인 사용자: " + memberId);
            }
        } finally {
            poolLock.unlock();
        }
    }
    public Map<String, Integer> getAllCategoryCounts() {
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        callCategoryCounts.forEach((category, atomic) -> {
            counts.put(category, atomic.get());
        });
        return counts;
    }

    private void scheduleMatchingTimeout(Long memberId, MatchType matchType) {
        long timeout = matchType == MatchType.CHAT ? CHAT_TIMEOUT_MINUTES : CALL_TIMEOUT_MINUTES;
        matchingScheduler.schedule(() -> removeFromMatchingPool(memberId), timeout, TimeUnit.MINUTES);
    }

    private void recordProcessingTime(long startTime) {
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
//        Metrics.timer("matching.process.time").record(durationMs, TimeUnit.MILLISECONDS);
        if (durationMs > MAX_PROCESSING_TIME_MS) {
            log.warn("매칭 처리 지연: {}ms", durationMs);
        }
    }

    private MatchResult createMatchResult(Member user1, Long user2Id, MatchType matchType) {
        Member user2 = getMemberOrThrow(user2Id);
        Long roomId = createRoom(user1, user2, matchType);
        removeFromMatchingPool(user1.getId());
        removeFromMatchingPool(user2Id);
        return new MatchResult(roomId, user2);
    }

    private void removeFromChatPool(Long memberId) {
        poolLock.lock();
        try {
            Set<String> interests = userToInterestsMap.remove(memberId);
            if (interests != null) {
                interests.forEach(interest -> {
                    Set<Long> users = chatInterestToUsersMap.get(interest);
                    if (users != null) {
                        users.remove(memberId);
                        if (users.isEmpty()) {
                            chatInterestToUsersMap.remove(interest);
                        }
                    }
                });
            }
            chatMatchCounter.set(calculateRealChatUserCount()); // 실시간 재계산
            broadcastMatchingCount(MatchType.CHAT); // 즉시 브로드캐스트
        } finally {
            poolLock.unlock();
        }
    }

    private void removeFromCallPool(Long memberId) {
        log.debug("Removing user {} from call pool", memberId);

        // 1. callUserContexts에서 제거
        CallMatchContext context = callUserContexts.remove(memberId);
        if (context == null) {
            log.warn("Call context not found for user {}", memberId);
            return;
        }

        // 2. 카테고리 맵에서 제거
        Set<Long> categoryUsers = callCategoryToUsersMap.get(context.category);
        if (categoryUsers != null) {
            synchronized (categoryUsers) {
                boolean removed = categoryUsers.remove(memberId);
                if (!removed) {
                    log.warn("User {} not found in category {}", memberId, context.category);
                }
                if (categoryUsers.isEmpty()) {
                    callCategoryToUsersMap.remove(context.category);
                }
            }
        }

        // 3. 카운트 업데이트
        updateCategoryCount(context.category, -1);
        callMatchCounter.decrementAndGet();
    }

    private void safeBroadcast(MatchType matchType, int retries) {
        try {
            broadcastMatchingCount(matchType);
        } catch (Exception e) {
            if (retries > 0) {
                log.warn("브로드캐스트 재시도 (남은 횟수: {})", retries);
                safeBroadcast(matchType, retries - 1);
            } else {
                log.error("브로드캐스트 실패", e);
            }
        }
    }

    private void broadcastMatchingCount(MatchType matchType) {
        int count = getCount(matchType);
        String topic = matchType == MatchType.CHAT
                ? "/topic/matching-count-chat"
                : "/topic/matching-count-call";

        log.debug("브로드캐스트 전송 - 타입: {}, 카운트: {}, 실제 풀 크기: {}",
                matchType,
                count,
                matchType == MatchType.CHAT
                        ? calculateRealChatUserCount()
                        : callCategoryToUsersMap.values().stream().mapToInt(Set::size).sum()
        );

        messagingTemplate.convertAndSend(topic, count);
    }

    public int getCount(MatchType matchType) {
        return matchType == MatchType.CHAT
                ? chatMatchCounter.get()
                : callMatchCounter.get();
    }

    private void updateCategoryCount(String category, int delta) {
        int newCount = callCategoryCounts
                .computeIfAbsent(category, k -> new AtomicInteger(0))
                .addAndGet(delta);
        messagingTemplate.convertAndSend("/topic/category-waiting-counts", Map.of(category, newCount));
    }

    private void sendMatchingSuccess(Member user1, Member user2, Long roomId, MatchType matchType) {
        log.info("매칭 성공 알림 전송 - 타입: {}, 사용자1: {}, 사용자2: {}, 방ID: {}",
                matchType, user1.getId(), user2.getId(), roomId);

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
                    Map.of("id", partner.getId(), "nickname", partner.getNickname()),
                    null
            );
        }
    }
    private int calculateRealChatUserCount() {
        return (int) chatInterestToUsersMap.values().stream()
                .flatMap(Set::stream)
                .distinct()
                .count();
    }
    private void handleRemovalFailure(Long memberId, MatchType matchType, Exception e) {
        log.error("매칭 풀 제거 실패 - 사용자: {}, 타입: {}", memberId, matchType, e);
        userMatchTypeMap.putIfAbsent(memberId, matchType);
//        Metrics.counter("matching.removal.failure").increment();
        throw new MatchingException("매칭 풀 제거 실패", e);
    }

    @PreDestroy
    public void cleanup() {
        log.info("매칭 서비스 종료 시작");
        matchingScheduler.shutdown();
        try {
            if (!matchingScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("강제 종료 실행");
                matchingScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("종료 처리 중 인터럽트 발생", e);
            matchingScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("매칭 서비스 종료 완료");
    }

    // ========== 내부 레코드/예외 ========== //

    private record MatchResult(Long roomId, Member matchedMember) {}

    public static class MatchingException extends RuntimeException {
        public MatchingException(String message) { super(message); }
        public MatchingException(String message, Throwable cause) { super(message, cause); }
    }

    @PostConstruct
    public void initDummyData() {
        log.info("더미 데이터 초기화 시작");
        List<String> dummyCategories = Arrays.asList("1", "2", "3", "4", "5");
        Random random = new Random();

        dummyCategories.forEach(category -> {
            callMatchCounter.incrementAndGet();
            int dummyCount = random.nextInt(10);
            callCategoryCounts.put(category, new AtomicInteger(dummyCount));
            log.debug("더미 데이터 추가 - 카테고리: {}, 인원: {}", category, dummyCount);
        });
        log.info("더미 데이터 초기화 완료");
    }

    /**
     * 매칭 실패 시 에러 응답 전송
     */
    private void sendMatchingError(Long userId, MatchType matchType, String errorMessage) {
        log.error("매칭 실패 알림 전송 - 사용자 ID: {}, 타입: {}, 오류: {}",
                userId, matchType, errorMessage);

        MatchingResponse errorResponse = MatchingResponse.error(matchType, errorMessage);
        messagingTemplate.convertAndSend("/queue/matching/" + userId, errorResponse);
    }

    /**
     * 매칭 처리 중 예외 발생 시 호출할 메서드
     */
    private void handleMatchingError(Long userId, MatchType matchType, Exception e) {
        log.error("매칭 처리 중 오류 - 사용자 ID: {}, 타입: {}", userId, matchType, e);

        String errorMessage = e instanceof MatchingException ?
                e.getMessage() : "매칭 처리 중 오류가 발생했습니다.";

        sendMatchingError(userId, matchType, errorMessage);
        removeFromMatchingPool(userId);
    }

}