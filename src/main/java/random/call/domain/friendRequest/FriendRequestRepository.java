package random.call.domain.friendRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.friendRequest.type.FriendRequestStatus;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);

    List<FriendRequest> findBySenderIdOrReceiverIdAndStatus(Long memberId1, Long memberId2, FriendRequestStatus status);
}
