package random.call.domain.friendRequest.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import random.call.domain.friendRequest.FriendRequest;
import random.call.domain.friendRequest.FriendRequestRepository;
import random.call.domain.friendRequest.type.FriendRequestStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;

    public void requestFriend(Long senderId, Long receiverId) {
        if (friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId).isPresent()) {
            throw new IllegalStateException("이미 요청을 보냈습니다.");
        }

        FriendRequest request = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);
    }

    public void acceptFriend(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않습니다."));

        if (!request.getReceiverId().equals(receiverId)) {
            throw new IllegalAccessError("본인의 요청만 수락할 수 있습니다.");
        }

        request.accept();
    }

    public List<Long> getFriendIds(Long memberId) {
        List<FriendRequest> accepted = friendRequestRepository.findBySenderIdOrReceiverIdAndStatus(
                memberId, memberId, FriendRequestStatus.ACCEPTED);

        return accepted.stream()
                .map(r -> r.getSenderId().equals(memberId) ? r.getReceiverId() : r.getSenderId())
                .toList();
    }
}
