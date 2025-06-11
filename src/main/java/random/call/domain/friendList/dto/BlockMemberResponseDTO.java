package random.call.domain.friendList.dto;

import lombok.Getter;
import random.call.domain.friendList.Friend;
import random.call.domain.member.Member;

import java.time.LocalDateTime;

@Getter
public class BlockMemberResponseDTO {
    private final Long id;
    private final LocalDateTime blockedAt;
    private final Long memberId;
    private final String nickname;

    public BlockMemberResponseDTO(Member member, Friend friend){
        this.id = friend.getId();
        this.blockedAt =friend.getBlockedAt();
        this.memberId =member.getId();
        this.nickname =member.getNickname();
    }
}
