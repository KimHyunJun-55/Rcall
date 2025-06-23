package random.call.domain.chat.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.chat.entity.ChatParticipant;
import random.call.domain.chat.entity.ChatRoom;
import random.call.domain.member.Member;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
    List<ChatParticipant> findByMemberId(Long memberId);

    Optional<ChatParticipant> findByChatRoomIdAndMemberIdNot(Long roomId, Long memberId);

    @Query("""
        SELECT cp1.chatRoom.id, m.id, m.nickname, m.profileImage
        FROM ChatParticipant cp1
        JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id
        JOIN Member m ON cp2.member.id = m.id
        WHERE cp1.member.id = :memberId
        AND cp2.member.id != :memberId
         AND cp1.isActive = true
    """)
    List<Object[]> findActiveRoomsWithMatchedNicknames(@Param("memberId") Long memberId);


    @Query("SELECT cp FROM ChatParticipant cp " +
            "WHERE cp.member.id = :memberId AND cp.chatRoom.id = :roomId")
    Optional<ChatParticipant> findByMemberIdAndRoomId(
            @Param("memberId") Long memberId,
            @Param("roomId") Long roomId
    );

    @Query("SELECT m.roomId, COUNT(m) " +
            "FROM ChatMessage m " +
            "WHERE m.roomId IN :roomIds " +
            "AND m.senderId != :memberId " +
            "AND (m.id > COALESCE((" +
            "   SELECT cp.lastReadMessageId FROM ChatParticipant cp " +
            "   WHERE cp.member.id = :memberId AND cp.chatRoom.id = m.roomId" +
            "), 0)) " +
            "GROUP BY m.roomId")
    List<Object[]> countUnreadMessagesByRooms(@Param("roomIds") List<Long> roomIds,
                                              @Param("memberId") Long memberId);

    default Map<Long, Integer> convertUnreadCounts(List<Long> roomIds, Long memberId) {
        return countUnreadMessagesByRooms(roomIds, memberId).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()
                ));
    }


    List<ChatParticipant> findByMember(Member member);


    void deleteByChatRoomIn(List<ChatRoom> chatRooms);

//    Optional<ChatParticipant> findByChatRoomIdAndMemberId(Long roomId, Long memberId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId AND cp.member.id = :memberId")
    Optional<ChatParticipant> findByChatRoomIdAndMemberIdWithLock(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") Long memberId
    );
}
