package random.call.domain.friendList.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.friendList.Friend;
import random.call.domain.friendList.dto.BlockMemberResponseDTO;
import random.call.domain.friendList.type.FriendStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend,Long> {
    boolean existsByMemberAAndMemberB(Long minId, Long maxId);


    @Query("SELECT f FROM Friend f WHERE (f.memberA = :userId OR f.memberB = :userId) AND f.status = :status")
    List<Friend> findActiveFriendsByUserId(@Param("userId") Long userId, @Param("status") FriendStatus status);

    boolean existsByMemberAAndMemberBAndStatus(Long minId, Long maxId, FriendStatus friendStatus);

    @Query("SELECT CASE WHEN f.memberA = :memberId THEN f.memberB ELSE f.memberA END " +
            "FROM Friend f " +
            "WHERE (f.memberA = :memberId OR f.memberB = :memberId) " +
            "AND f.status = 'BLOCKED'")
    List<Long> findBlockedMembersByMemberId(@Param("memberId") Long memberId);

    Optional<Friend> findByMemberAAndMemberB(Long minId, Long maxId);

    /**
     * 내가 차단한 사용자 ID 목록 조회
     * @param memberId 현재 사용자 ID
     * @return 내가 차단한 사용자 ID 리스트
     */
    @Query("SELECT new random.call.domain.friendList.dto.BlockMemberResponseDTO(" +
            "m, f) " +
            "FROM Friend f " +
            "JOIN Member m ON (f.memberA = :memberId AND m.id = f.memberB) OR " +
            "                (f.memberB = :memberId AND m.id = f.memberA) " +
            "WHERE (f.memberA = :memberId OR f.memberB = :memberId) " +
            "AND f.status = 'BLOCKED' " +
            "AND f.blockedBy = :memberId")
    List<BlockMemberResponseDTO> findBlockedUsers(@Param("memberId") Long memberId);

    List<Friend> findByMemberAOrMemberB(Long memberId,Long id);
}
