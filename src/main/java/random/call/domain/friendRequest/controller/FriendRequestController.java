package random.call.domain.friendRequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.friendRequest.service.FriendRequestService;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friend")
public class FriendRequestController {

    private final FriendRequestService friendService;

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<Void> sendRequest(@AuthenticationPrincipal JwtUserDetails user, @PathVariable Long receiverId) {
        friendService.requestFriend(user.id(), receiverId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Void> acceptRequest(@AuthenticationPrincipal JwtUserDetails user, @PathVariable Long requestId) {
        friendService.acceptFriend(requestId, user.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public ResponseEntity<List<Long>> getFriends(@AuthenticationPrincipal JwtUserDetails user) {
        return ResponseEntity.ok(friendService.getFriendIds(user.id()));
    }
}
