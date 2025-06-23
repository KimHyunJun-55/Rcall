package random.call.domain.call.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.match.MatchType;
import random.call.domain.match.service.MatchService;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/call")
@RequiredArgsConstructor
public class CallController {

    private final MatchService matchService;



    @PostMapping("/cancel")
    public ResponseEntity<Boolean> cancelMatching(@AuthenticationPrincipal JwtUserDetails jwtUserDetails) {
        matchService.removeFromMatchingPool(jwtUserDetails.id());
        return ResponseEntity.ok(true);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> matchCount(){
        return ResponseEntity.ok(matchService.getCount(MatchType.CALL));
    }

    @GetMapping("/category-counts")
    public ResponseEntity<Map<String, Integer>> getCategoryCounts() {
        return ResponseEntity.ok(matchService.getAllCategoryCounts());
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalCount(){
        return ResponseEntity.ok(148);
    }


}
