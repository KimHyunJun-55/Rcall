package random.call.domain.reply;


import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Boolean> createReply(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long feedId,
                                               @RequestBody ReplyRequest request) {
        replyService.createReply(userDetails.member(), feedId, request);
        return ResponseEntity.ok(true);
    }

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<List<ReplyResponse>> getReplies(@PathVariable Long feedId) {
        return ResponseEntity.ok(replyService.getReplies(feedId));
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
