package random.call.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId AND m.createdAt > :after")
    List<ChatMessage> findRecentMessages(
            @Param("roomId") Long roomId,
            @Param("after") LocalDateTime after);

    Optional<ChatMessage> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId);

    @Query("""
    SELECT m
    FROM ChatMessage m
    WHERE m.roomId IN :roomIds AND m.createdAt IN (
        SELECT MAX(m2.createdAt)
        FROM ChatMessage m2
        WHERE m2.roomId IN :roomIds
        GROUP BY m2.roomId
    )
""")
    List<ChatMessage> findLatestMessagesByRoomIds(@Param("roomIds") List<Long> roomIds);

}