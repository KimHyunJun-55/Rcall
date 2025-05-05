package random.call.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
    List<ChatParticipant> findByMemberId(Long memberId);

    Optional<ChatParticipant> findByChatRoomIdAndMemberIdNot(Long roomId, Long memberId);

    @Query("""
    SELECT cp1.chatRoom.id, m.nickname
    FROM ChatParticipant cp1
    JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id
    JOIN Member m ON cp2.member.id = m.id
    WHERE cp1.member.id = :memberId AND cp2.member.id != :memberId
""")
    List<Object[]> findRoomIdsAndMatchedNicknames(@Param("memberId") Long memberId);


}
