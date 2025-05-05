package random.call.domain.friendList;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> addFriend(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        friendService.addFriend(userDetails.id(), targetId);
        return ResponseEntity.ok().build();
    }
}
