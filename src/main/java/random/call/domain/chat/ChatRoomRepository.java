package random.call.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
}
