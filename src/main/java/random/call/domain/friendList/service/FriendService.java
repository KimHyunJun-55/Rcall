package random.call.domain.friendList.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.friendList.Friend;
import random.call.domain.friendList.dto.BlockMemberResponseDTO;
import random.call.domain.friendList.dto.FriendResponseDTO;
import random.call.domain.friendList.repository.FriendRepository;
import random.call.domain.friendList.type.FriendStatus;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Transactional
    public void blockFriend(Long memberAId, Long targetId) {
        Long minId = Math.min(memberAId, targetId);
        Long maxId = Math.max(memberAId, targetId);

        Friend friend = friendRepository.findByMemberAAndMemberB(minId, maxId)
                .orElseGet(() -> {
                    Friend newFriend = Friend.builder()
                            .memberA(minId)
                            .memberB(maxId)
                            .status(FriendStatus.BLOCKED)
                            .blockedBy(memberAId)
                            .build();
                    return friendRepository.save(newFriend); // 저장이 필요
                });

        friend.blockUpdate(); // 차단 상태로 변경
    }

    // 내가 차단한 유저 목록 조회
    @Transactional(readOnly = true)
    public List<BlockMemberResponseDTO> getBlockedUsers(Long memberId) {
        return friendRepository.findBlockedUsers(memberId);
    }

    public void unBlockMember(Long targetId) {

        Friend friend = friendRepository.findById(targetId).orElseThrow(()-> new EntityNotFoundException("해당 데이터가 없습니다."));

        friendRepository.delete(friend);
        log.info("차단해제");



    }

    public void deleteFriendList(Member member){
        List<Friend> friends = friendRepository.findByMemberAOrMemberB(member.getId(),member.getId());
        friendRepository.deleteAll(friends);
        log.info("{} 회원의 친구목록 삭제 프로세스 완료",member.getId());


    }
}
