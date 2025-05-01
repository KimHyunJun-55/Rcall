package random.call.domain.feed.controller;


import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.feed.dto.FeedRequest;
import random.call.domain.feed.dto.FeedResponse;
import random.call.domain.feed.service.FeedService;
import random.call.global.security.userDetails.CustomUserDetails;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;


    @GetMapping("/feedId")
    public ResponseEntity<FeedResponse> getFeed(@PathVariable("feedId")Long feedId){

        FeedResponse response = feedService.getFeed(feedId);

        return ResponseEntity.ok(response);

    }

    @GetMapping("")
    public ResponseEntity<Page<FeedResponse>> getFeeds(Pageable pageable) {
        Page<FeedResponse> response = feedService.getFeeds(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    public ResponseEntity<Boolean> crateFeed(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody  @Valid FeedRequest request){

        feedService.createFeed(userDetails.member(),request);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/feedId")
    public ResponseEntity<Boolean> updateFeed(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FeedRequest request){

        feedService.updateFeed(userDetails.member(),1L,request);
        return ResponseEntity.ok(true);
    }
}
