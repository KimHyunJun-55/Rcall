package random.call.domain.like;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.like.dto.LikeToggleResponse;
import random.call.global.security.userDetails.CustomUserDetails;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{feedId}")
    public ResponseEntity<LikeToggleResponse> likeToggle(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable("feedId") Long feedId
    ) {
        LikeToggleResponse like = likeService.likeToggle(userDetails.id(), feedId);
        return ResponseEntity.ok(like);
    }
}
