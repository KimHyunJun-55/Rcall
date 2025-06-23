package random.call.domain.friendRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.friendRequest.type.FriendRequestStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);

    List<FriendRequest> findBySenderIdOrReceiverIdAndStatus(Long memberId1, Long memberId2, FriendRequestStatus status);


    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "(fr.senderId = :id1 AND fr.receiverId = :id2) OR " +
            "(fr.senderId = :id2 AND fr.receiverId = :id1)")
    Optional<FriendRequest> findFriendRequestBetween(Long id1, Long id2);

    //내가 친구신청한 목록
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.senderId = :id AND fr.status = :status")
    List<FriendRequest> findByRequestMember(@Param("id") Long id, @Param("status") FriendRequestStatus status);


    //나를 신청한 목록
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiverId = :id AND fr.status = :status")
    List<FriendRequest> findByRequestReceive(@Param("id") Long id, @Param("status") FriendRequestStatus status);

    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiverId = :receiverId AND fr.senderId =:senderId ")
    Optional<FriendRequest> findByReceiverIdAndSendId(@Param("senderId") Long senderId,@Param("receiverId")Long receiverId);

    List<FriendRequest> findBySenderIdOrReceiverId(Long id, Long id1);
}
