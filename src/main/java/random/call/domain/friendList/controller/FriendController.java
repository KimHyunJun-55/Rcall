package random.call.domain.friendList.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.friendList.dto.FriendResponseDTO;
import random.call.domain.friendList.service.FriendService;
import random.call.domain.friendList.dto.BlockMemberResponseDTO;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("")
    public ResponseEntity<?> getFriends (@AuthenticationPrincipal JwtUserDetails jwtUserDetails){

        Long memberId = jwtUserDetails.id();
        List<FriendResponseDTO> friendResponseDTOList = friendService.getFriends(memberId);
        return ResponseEntity.ok(friendResponseDTOList);

    }

    @PostMapping("/block/{targetId}")
    public ResponseEntity<Void> blockMember(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        friendService.blockFriend(userDetails.id(), targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/block-member")
    public ResponseEntity<List<BlockMemberResponseDTO>> getBlockMember(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){

        return ResponseEntity.ok(friendService.getBlockedUsers(jwtUserDetails.id()));

    }
    @DeleteMapping("/block-member/{targetId}")
    public ResponseEntity<Boolean> unBlockMember(
//            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            @PathVariable("targetId") Long targetId
    ){

        friendService.unBlockMember(targetId);

        return ResponseEntity.ok(true);

    }


}
