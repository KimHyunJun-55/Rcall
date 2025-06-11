package random.call.domain.chat;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.chat.dto.ChatMessageDto;
import random.call.domain.chat.dto.ChatRoomHistory;
import random.call.domain.chat.entity.ChatMessage;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.repository.ChatMessageRepository;
import random.call.domain.chat.repository.ChatParticipantRepository;
import random.call.domain.chat.repository.ChatRoomRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;


    //채팅방 기록 불러오기
    @Transactional(readOnly = true)
    public List<ChatRoomHistory> getChatRoomHistory(Long memberId) {
        // 1. 사용자가 속한 채팅방 ID와 상대방 닉네임 조회
        List<Object[]> participantResults = chatParticipantRepository.findRoomIdsAndMatchedNicknames(memberId);

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
                .tempId(dto.getTempId()) // 클라이언트의 임시 ID 전달
                .build();

        messagingTemplate.convertAndSend("/topic/chat/room/" + dto.getRoomId(), response);
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



}
