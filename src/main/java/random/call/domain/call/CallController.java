package random.call.domain.call;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.match.service.MatchService;
import random.call.global.security.userDetails.JwtUserDetails;


@RestController
@RequestMapping("/api/v1/call")
@RequiredArgsConstructor
public class CallController {

    private final MatchService matchService;

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelMatching(@AuthenticationPrincipal JwtUserDetails jwtUserDetails) {
        matchService.removeFromMatchingPool(jwtUserDetails.id());
        return ResponseEntity.ok().build();
    }
}
