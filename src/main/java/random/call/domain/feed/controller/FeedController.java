package random.call.domain.feed.controller;


import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.feed.dto.*;
import random.call.domain.feed.service.FeedService;
import random.call.global.security.userDetails.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import random.call.global.security.userDetails.JwtUserDetails;


@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    //피드 조회 (페이징)
    @GetMapping("")
    public ResponseEntity<Page<FeedResponse>> getFeeds(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, Pageable pageable) {

        Page<FeedResponse> response = feedService.getFeeds(jwtUserDetails.id(),pageable);
        return ResponseEntity.ok(response);
    }
    //피드조회(페이징/심플)
    @GetMapping("/simple")
    public ResponseEntity<Page<FeedSimpleResponseDTO>> getFeedsSimple(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, Pageable pageable) {
        Page<FeedSimpleResponseDTO> response = feedService.getFeedsSimple(jwtUserDetails.id(),pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/before/{feedId}")
    public ResponseEntity<Page<FeedResponse>> getFeedByFeedIdBefore(@PathVariable("feedId")Long feedId,@AuthenticationPrincipal JwtUserDetails jwtUserDetails, Pageable pageable) {
        System.out.println("비포요청");
        Page<FeedResponse> response = feedService.getFeedByFeedIdBefore(jwtUserDetails.id(),feedId,pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/after/{feedId}")
    public ResponseEntity<Page<FeedResponse>> getFeedByFeedIdAfter(@PathVariable("feedId")Long feedId,@AuthenticationPrincipal JwtUserDetails jwtUserDetails, Pageable pageable) {
        System.out.println("에프터 요청");
        Page<FeedResponse> response = feedService.getFeedByFeedIdAfter(jwtUserDetails.id(),feedId,pageable);
        return ResponseEntity.ok(response);
    }
    //피드 단일조회 FEED_ID
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponse> getFeed(@PathVariable("feedId")Long feedId, @AuthenticationPrincipal JwtUserDetails jwtUserDetails) {
        FeedResponse response = feedService.getFeed(jwtUserDetails.id(),feedId);
        return ResponseEntity.ok(response);
    }

    //해당유저의 피드들
    @GetMapping("/member/{memberId}")
    public ResponseEntity<Page<FeedResponse>> getFeedsByMemberID(
            @PathVariable("memberId")Long memberId,
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            Pageable pageable) {
        Page<FeedResponse> response = feedService.getFeedsByMemberId(memberId,jwtUserDetails.id(),pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("simple/{memberId}")
    public ResponseEntity<Page<FeedRequestByMemberIdDTO>> getSimpleFeedsByMemberID(
            @PathVariable("memberId")Long memberId,
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            Pageable pageable) {
        Page<FeedRequestByMemberIdDTO> response = feedService.getSimpleFeedsByMemberId(memberId,jwtUserDetails.id(),pageable);
        return ResponseEntity.ok(response);
    }
    //피드작성
    @PostMapping("")
    public ResponseEntity<Boolean> crateFeed(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FeedRequest request){

        feedService.createFeed(userDetails.member(),request);
        return ResponseEntity.ok(true);
    }
    //피드수정
    @PutMapping("/{feedId}")
    public ResponseEntity<Boolean> updateFeed(@PathVariable("feedId")Long feedId,@AuthenticationPrincipal JwtUserDetails jwtUserDetails, @RequestBody FeedRequest request){

        feedService.updateFeed(jwtUserDetails.id(),feedId,request);
        return ResponseEntity.ok(true);
    }
}
