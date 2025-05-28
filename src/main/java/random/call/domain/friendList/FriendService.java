package random.call.domain.friendList;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.friendList.type.FriendStatus;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

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
                .build();

        friendRepository.save(friend);
    }

    @Transactional
    public List<FriendResponseDTO> getFriends(Long memberId) {

        List<Friend> friends = friendRepository.findActiveFriendsByUserId(memberId, FriendStatus.ACTIVE);

        return friends.stream()
                .map(f -> {
                    Long otherMemberId = f.getMemberA().equals(memberId) ? f.getMemberB() : f.getMemberA();
                    Optional<Member> member = memberRepository.findById(otherMemberId);
                    return member.map(FriendResponseDTO::new).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
