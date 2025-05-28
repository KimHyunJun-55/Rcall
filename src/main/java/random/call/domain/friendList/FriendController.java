package random.call.domain.friendList;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.member.dto.FriendProfileResponseDTO;
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

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> addFriend(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        friendService.addFriend(userDetails.id(), targetId);
        return ResponseEntity.ok().build();
    }
}
