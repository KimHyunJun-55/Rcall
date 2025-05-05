package random.call.domain.friendList;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import random.call.domain.friendList.type.FriendStatus;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;

    public void addFriend(Long memberAId, Long memberBId) {
        Long minId = Math.min(memberAId, memberBId);
        Long maxId = Math.max(memberAId, memberBId);

        boolean exists = friendRepository.existsByMemberAAndMemberB(minId, maxId);
        if (exists) {
            throw new IllegalStateException("이미 친구 관계가 존재합니다.");
        }

        Friend friend = Friend.builder()
                .memberA(minId)
                .memberB(maxId)
                .status(FriendStatus.ACTIVE)
                .isBlocked(false)
                .build();

        friendRepository.save(friend);
    }
}
