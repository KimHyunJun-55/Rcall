package random.call.domain.chat;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import random.call.domain.chat.dto.ChatExitEvent;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.dto.ChatRoomHistory;
import random.call.domain.chat.entity.ChatMessage;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.chat.repository.ChatMessageRepository;
import random.call.domain.chat.repository.ChatParticipantRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.member.Member;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {


    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;


    //채팅방 기록 불러오기
    @Transactional(readOnly = true)
    public List<ChatRoomHistory> getChatRoomHistory(Long memberId) {
        // 1. 사용자가 속한 채팅방 ID와 상대방 닉네임 조회
        List<Object[]> participantResults = chatParticipantRepository.findActiveRoomsWithMatchedNicknames(memberId);

        // 2. 채팅방 ID 리스트 추출
        List<Long> roomIds = participantResults.stream()
                .map(r -> (Long) r[0])
                .toList();

        // 3. 채팅방별 최신 메시지 일괄 조회 (기존 로직 유지)
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByRoomIds(roomIds);

        // 4. 채팅방별 안읽은 메시지 수 일괄 조회 (신규 추가)
        Map<Long, Integer> unreadCounts = chatParticipantRepository
                .convertUnreadCounts(roomIds, memberId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 5. 결과 조합
        return participantResults.stream()
                .map(r -> {
                    Long roomId = (Long) r[0];
                    Long matchMemberId = (Long)r[1];
                    String nickname = (String) r[2];
                    String profileImage = (String) r[3];
                    ChatMessage latestMessage = latestMessages.stream()
                            .filter(m -> m.getRoomId().equals(roomId))
                            .findFirst()
                            .orElse(null);

                    return ChatRoomHistory.builder()
                            .RoomId(roomId)
                            .memberId(matchMemberId)
                            .memberNickname(nickname)
                            .profileImage(profileImage)
                            .lastMessage(latestMessage != null ? latestMessage.getContent() : "")
                            .lastTime(latestMessage != null ?
                                    latestMessage.getCreatedAt().format(formatter) : "")
                            .unreadCount(unreadCounts.getOrDefault(roomId, 0)) // 안읽은 메시지 수 추가
                            .build();
                })
                .toList();
    }

    @Transactional
    public void extracted(ChatMessageDto dto, Long userId, String nickname) {
        ChatMessage message = ChatMessage.builder()
                .senderId(userId)
                .content(dto.getContent())
                .roomId(dto.getRoomId())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // 3. 프론트엔드 요구사항에 맞는 응답 구성
        ChatMessageDto response = ChatMessageDto.builder()
                .id(saved.getId().toString())
                .senderId(saved.getSenderId())
                .sender(nickname)
                .content(saved.getContent())
                .roomId(saved.getRoomId())
                .createdAt(saved.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME))
                .tempId(dto.getTempId())
                .build();


        messagingTemplate.convertAndSend("/topic/chat/room/" + dto.getRoomId(), response);
        messagingTemplate.convertAndSend("/topic/chat/user/"+dto.getTargetId() , response);
    }

    @Transactional
    public void markMessagesAsRead(Long roomId, Long readerId, List<Long> messageIds) {
        // 1. Optional로 안전하게 조회
        ChatParticipant participant = chatParticipantRepository
                .findByMemberIdAndRoomId(readerId, roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 참여자 없음 - memberId: " + readerId + ", roomId: " + roomId));

        // 2. 메시지 ID 유효성 검사
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        // 3. 최대 메시지 ID 계산
        Long maxMessageId = messageIds.stream()
                .max(Long::compareTo)
                .orElse(participant.getLastReadMessageId());

        // 4. lastReadMessageId 업데이트 (조건부)
        if (maxMessageId != null &&
                (participant.getLastReadMessageId() == null ||
                        maxMessageId > participant.getLastReadMessageId())) {
            participant.setLastReadMessageId(maxMessageId);
            chatParticipantRepository.save(participant);
        }

        // 5. 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/chat/room/" + roomId + "/read",
                Map.of(
                        "readerId", readerId,
                        "messageIds", messageIds
                )
        );
    }


    public void deleteAllChat(Member member) {
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByMember(member);

        List<ChatRoom> chatRooms = chatParticipants.stream()
                .map(ChatParticipant::getChatRoom)
                .distinct()
                .toList();

        List<Long> roomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        chatMessageRepository.deleteByRoomIdIn(roomIds);
        log.info("{} 회원의 채팅메시지 삭제 프로세스 완료",member.getId());


        chatParticipantRepository.deleteByChatRoomIn(chatRooms);
        log.info("{} 회원의 채팅참여자 삭제 프로세스 완료",member.getId());

        chatRoomRepository.deleteAll(chatRooms);
        log.info("{} 회원의 채팅방 삭제 프로세스 완료",member.getId());
    }


    @Transactional
    public void exitChatRoom(Long roomId, Long memberId) {
        // 1. 참여자 상태 업데이트
        ChatParticipant participant = chatParticipantRepository
                .findByChatRoomIdAndMemberIdWithLock(roomId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("참여자 없음"));

        participant.exitRoom();

        // 2. 시스템 메시지 저장
//        ChatMessage exitMessage = createSystemMessage(roomId);
//        chatMessageRepository.save(exitMessage);

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 없음"));
        room.setActive(false);

        // 3. 트랜잭션 커밋 후 메시지 전송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(
                        "/topic/chat/events/" + roomId,
                        new ChatExitEvent(
                                "ROOM_STATE",
                                false,
                                memberId,
                                LocalDateTime.now()
                        )
                );
            }
        });
    }



    private ChatMessage createSystemMessage(Long roomId) {
        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(null)
                .content("[시스템] 상대방이 퇴장하셨습니다")
                .build();
    }
}
