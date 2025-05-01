package random.call.domain.match;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.VoiceMatchResponse;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/request")
    public ResponseEntity<VoiceMatchResponse> requestMatch(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestBody MatchRequest request
    ) {
        VoiceMatchResponse response = matchService.processMatchRequest(userDetails.id(), request);
        return ResponseEntity.ok(response);
    }

}