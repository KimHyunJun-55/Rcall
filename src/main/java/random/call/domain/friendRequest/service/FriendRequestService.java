package random.call.domain.friendRequest.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.friendList.FriendService;
import random.call.domain.friendRequest.FriendRequest;
import random.call.domain.friendRequest.FriendRequestRepository;
import random.call.domain.friendRequest.type.FriendRequestStatus;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.dto.FriendProfileResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;
    private final FriendService friendService; // 친구 관계 서비스 주입

    //친구신청
    @Transactional
    public void requestFriend(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        Optional<FriendRequest> existingRequestOpt = friendRequestRepository
                .findBySenderIdAndReceiverId(senderId, receiverId);

        if (existingRequestOpt.isPresent()) {
            FriendRequest existingRequest = existingRequestOpt.get();

            switch (existingRequest.getStatus()) {
                case PENDING:
                case ACCEPTED:
                case REJECTED:
                    throw new IllegalStateException("이미 요청을 보냈거나, 수락/거절된 요청입니다.");
                case CANCELLED:
                    // 상태만 되돌려서 재요청 처리
                    existingRequest.pending();
                    friendRequestRepository.save(existingRequest);
                    return;
            }
        }

        FriendRequest request = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);
    }


    //친구신청취소
    @Transactional
    public void requestFriendCancel(Long senderId, Long receiverId) {

        FriendRequest friendRequest =friendRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId)
                .orElseThrow(() ->  new EntityNotFoundException("해당요청을 찾을 수 없습니다."));

        friendRequest.cancel();

        friendRequestRepository.delete(friendRequest);
    }

    
    //친구신청 수락
    @Transactional
    public void acceptFriend(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findByReceiverIdAndSendId(requestId,receiverId)
                .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않습니다."));

        if (!request.getReceiverId().equals(receiverId)) {
            throw new IllegalAccessError("본인의 요청만 수락할 수 있습니다.");
        }

        request.accept();

        // 실제 친구 관계 테이블에 저장
        friendService.addFriend(request.getSenderId(), request.getReceiverId());
    }

    public List<Long> getFriendIds(Long memberId) {
        List<FriendRequest> accepted = friendRequestRepository.findBySenderIdOrReceiverIdAndStatus(
                memberId, memberId, FriendRequestStatus.ACCEPTED);

        return accepted.stream()
                .map(r -> r.getSenderId().equals(memberId) ? r.getReceiverId() : r.getSenderId())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendProfileResponseDTO> checkFriendRequest(Long id) {
        List<FriendRequest> requests = friendRequestRepository.findByRequestMember(id, FriendRequestStatus.PENDING);

        return requests.stream()
                .map(req -> {
                    Optional<Member> optional = memberRepository.findById(req.getReceiverId());
                    return optional.map(member -> new FriendProfileResponseDTO(member, req.getCreatedAt()));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FriendProfileResponseDTO> checkFriendReceive(Long id) {
        List<FriendRequest> requests = friendRequestRepository.findByRequestReceive(id, FriendRequestStatus.PENDING);

        return requests.stream()
                .map(req -> {
                    Optional<Member> optional = memberRepository.findById(req.getSenderId());
                    return optional.map(member -> new FriendProfileResponseDTO(member, req.getCreatedAt()));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Transactional
    public void declineFriend(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findByReceiverIdAndSendId(requestId,receiverId)
                .orElseThrow(() -> new EntityNotFoundException("요청이 존재하지 않습니다."));
        if (!request.getReceiverId().equals(receiverId)) {
            throw new IllegalAccessError("본인의 요청만 수락할 수 있습니다.");
        }
friendRequestRepository.delete(request);
    }

}
