package random.call.domain.friendRequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.friendRequest.service.FriendRequestService;
import random.call.domain.member.dto.FriendProfileResponseDTO;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friend")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    //내가 친구신청한 유저목록
    @GetMapping("/checkRequest")
    public ResponseEntity<List<FriendProfileResponseDTO>> checkFriendRequest(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){
        List<FriendProfileResponseDTO> friendProfileResponseDTOS = friendRequestService.checkFriendRequest(jwtUserDetails.id());

        return ResponseEntity.ok(friendProfileResponseDTOS);
    }

    //내가 받은요청
    @GetMapping("/checkReceive")
    public ResponseEntity<List<FriendProfileResponseDTO>> checkFriendReceive(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){
        List<FriendProfileResponseDTO> friendProfileResponseDTOS = friendRequestService.checkFriendReceive(jwtUserDetails.id());

        return ResponseEntity.ok(friendProfileResponseDTOS);
    }

    //친구신청
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<Void> sendRequest(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, @PathVariable Long receiverId) {
        friendRequestService.requestFriend(jwtUserDetails.id(), receiverId);
        return ResponseEntity.ok().build();
    }

    //친구신청취소
    @GetMapping("/request/cancel/{receiverId}")
    public ResponseEntity<Void> sendRequestCancel(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, @PathVariable Long receiverId) {
        friendRequestService.requestFriendCancel(jwtUserDetails.id(), receiverId);
        return ResponseEntity.ok().build();
    }

    //친구요청 수락
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Void> acceptRequest(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, @PathVariable Long requestId) {
        friendRequestService.acceptFriend(requestId, jwtUserDetails.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<Long>> getFriends(@AuthenticationPrincipal JwtUserDetails jwtUserDetails) {
        return ResponseEntity.ok(friendRequestService.getFriendIds(jwtUserDetails.id()));
    }
}
