package random.call.domain.chat;


import org.springframework.stereotype.Service;
import random.call.domain.chat.entity.ChatRoom;

import java.util.*;

@Service
public class ChatService {
    private Queue<String> waitingQueue = new LinkedList<>();
    private Map<String, ChatRoom> activeRooms = new HashMap<>();

    // 대기열에 사용자 추가
    public void addUserToQueue(String userId) {
        waitingQueue.offer(userId);
        if (waitingQueue.size() >= 2) {
            // 두 명의 사용자 매칭
            String user1 = waitingQueue.poll();
            String user2 = waitingQueue.poll();
            createChatRoom(user1, user2);
        }
    }

    private void createChatRoom(String user1, String user2) {
        // 룸 아이디 생성: 예: "room_user1_user2" 또는 UUID
        String roomId = "room_" + user1 + "_" + user2; // 또는 UUID.randomUUID().toString();
        List<String> participants = Arrays.asList(user1, user2);

        // ChatRoom 객체 생성
        ChatRoom chatRoom = ChatRoom
                .builder()
                .roomId(roomId)
                .participants(participants)
                .build();

        // 룸을 활성화된 방 목록에 추가
        activeRooms.put(roomId, chatRoom);

        // 사용자들에게 해당 roomId 구독
        // 예: "/topic/room/{roomId}" 구독
        System.out.println("매칭된 룸: " + roomId + " 참여자들: " + participants);
    }



}
