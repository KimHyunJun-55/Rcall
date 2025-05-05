package random.call.domain.match;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.match.dto.MatchRequest;
import random.call.domain.match.dto.VoiceMatchResponse;
import random.call.domain.match.service.MatchService;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/api/v1/match")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;

//    @PostMapping("/request")
//    public ResponseEntity<VoiceMatchResponse> requestMatch(
//            @AuthenticationPrincipal JwtUserDetails userDetails,
//            @RequestBody MatchRequest request
//    ) {
//        VoiceMatchResponse response = matchService.processMatchRequest(userDetails.id(), request);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/roomId")
    public ResponseEntity<?> getMatch() {
        try {
            Thread.sleep(23000); // 10초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interrupted");
        }
        log.info("룸아이디요청");
        return ResponseEntity.ok("response");
    }


}