package random.call.domain.reply;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.reply.dto.ReplyRequest;
import random.call.domain.reply.dto.ReplyResponse;
import random.call.domain.reply.service.ReplyService;
import random.call.global.security.userDetails.CustomUserDetails;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed/{feedId}/replies")
public class ReplyController {

    private final ReplyService replyService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<ReplyResponse> createReply(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long feedId,
                                               @RequestBody ReplyRequest request) {
        ReplyResponse reply =replyService.createReply(userDetails.member(), feedId, request);
        return ResponseEntity.ok(reply);
    }

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<Page<ReplyResponse>> getReplies(
            @PathVariable Long feedId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<ReplyResponse> responses = replyService.getReplies(feedId,pageable);

        return ResponseEntity.ok(responses);
    }


    // 댓글 수정
    @PutMapping("/{replyId}")
    public ResponseEntity<Boolean> updateReply(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long replyId,
                                               @RequestBody ReplyRequest request) {
        replyService.updateReply(userDetails.member(), replyId, request);
        return ResponseEntity.ok(true);
    }

    // 댓글 삭제
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Boolean> deleteReply(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long replyId) {
        replyService.deleteReply(userDetails.member(), replyId);
        return ResponseEntity.ok(true);
    }
}
