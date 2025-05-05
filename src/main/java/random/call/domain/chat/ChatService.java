package random.call.domain.chat;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.chat.dto.ChatRoomHistory;
import random.call.domain.chat.entity.ChatMessage;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.repository.ChatMessageRepository;
import random.call.domain.chat.repository.ChatParticipantRepository;
import random.call.domain.chat.repository.ChatRoomRepository;
import random.call.domain.member.Member;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {


    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;


    @Transactional(readOnly = true)
    public List<ChatRoomHistory> getChatRoomHistory(Long memberId) {
        List<Object[]> results = chatParticipantRepository.findRoomIdsAndMatchedNicknames(memberId);

        List<Long> roomIds = results.stream()
                .map(r -> (Long) r[0])
                .toList();

        Map<Long, String> roomToNickname = results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (String) r[1]
                ));

        List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessagesByRoomIds(roomIds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Map<Long, ChatMessage> roomToLatestMessage = latestMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        m -> m
                ));

        return roomIds.stream()
                .map(roomId -> {
                    ChatMessage msg = roomToLatestMessage.get(roomId);
                    return ChatRoomHistory.builder()
                            .RoomId(roomId)
                            .memberNickname(roomToNickname.get(roomId))
                            .lastMessage(msg != null ? msg.getContent() : "")
                            .lastTime(msg != null ? msg.getCreatedAt().format(formatter) : "")
                            .build();
                })
                .toList();
    }



}
