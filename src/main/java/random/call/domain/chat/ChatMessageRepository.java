package random.call.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.createdAt > :after")
    List<ChatMessage> findRecentMessages(
            @Param("roomId") String roomId,
            @Param("after") LocalDateTime after);
}