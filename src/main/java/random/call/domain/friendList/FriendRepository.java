package random.call.domain.friendList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.friendList.type.FriendStatus;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend,Long> {
    boolean existsByMemberAAndMemberB(Long minId, Long maxId);


    @Query("SELECT f FROM Friend f WHERE (f.memberA = :userId OR f.memberB = :userId) AND f.status = :status")
    List<Friend> findActiveFriendsByUserId(@Param("userId") Long userId, @Param("status") FriendStatus status);

    boolean existsByMemberAAndMemberBAndStatus(Long minId, Long maxId, FriendStatus friendStatus);
}
