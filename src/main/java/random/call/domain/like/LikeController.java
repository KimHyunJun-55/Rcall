package random.call.domain.like;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.global.security.userDetails.CustomUserDetails;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/api/v1/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/feedId")
    public ResponseEntity<Boolean> likeToggle(@AuthenticationPrincipal JwtUserDetails userDetails, @PathVariable("feedId") Long feedId){

        boolean like = likeService.likeToggle(userDetails.id(),feedId);
        return ResponseEntity.ok(like);

    }
}
