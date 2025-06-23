package random.call.domain.reply.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.reply.dto.ReplyRequest;
import random.call.domain.reply.dto.ReplyResponse;
import random.call.domain.reply.dto.ReplyUpdateRequest;
import random.call.domain.reply.service.ReplyService;
import random.call.global.security.userDetails.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/replies") // 계층 구조 제거
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 등록 (피드 ID는 요청 본문으로)
    @PostMapping
    public ResponseEntity<ReplyResponse> createReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReplyRequest request) { // feedId 포함
        return ResponseEntity.ok(
                replyService.createReply(userDetails.member(), request)
        );
    }

    // 피드별 댓글 목록 조회
    @GetMapping("/by-feed/{feedId}")
    public ResponseEntity<Page<ReplyResponse>> getRepliesByFeed(
            @PathVariable Long feedId,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(
                replyService.getReplies(feedId, pageable)
        );
    }

    // 댓글 수정
    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyResponse> updateReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long replyId,
            @RequestBody ReplyUpdateRequest request) {
        ReplyResponse response =replyService.updateReply(userDetails.member(), replyId, request);
        return ResponseEntity.ok(response); // 204 No Content
    }

    // 댓글 삭제
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long replyId) {
        replyService.deleteReply(userDetails.member(), replyId);
        return ResponseEntity.noContent().build();
    }
}
