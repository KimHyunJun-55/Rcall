package random.call.domain.reply.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;
import random.call.domain.member.Member;
import random.call.domain.reply.Reply;
import random.call.domain.reply.dto.ReplyRepository;
import random.call.domain.reply.dto.ReplyRequest;
import random.call.domain.reply.dto.ReplyResponse;
import random.call.domain.reply.dto.ReplyUpdateRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public ReplyResponse createReply(Member member, ReplyRequest request) {
        Feed feed = feedRepository.findById(request.getFeedId())
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        Reply reply = Reply.builder()
                .feed(feed)
                .writer(member)
                .content(request.getContent())
                .build();

        replyRepository.save(reply);
        feed.addComment(); // 댓글 수 증가
        feedRepository.save(feed);

        return new ReplyResponse(reply);
    }

    @Transactional
    public ReplyResponse updateReply(Member member, Long replyId, ReplyUpdateRequest request) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        if (!reply.getWriter().getId().equals(member.getId())) {
            throw new AccessDeniedException("You can only edit your own reply.");
        }

        reply.updateContent(request.content());
        return new ReplyResponse(reply);
    }

    @Transactional
    public void deleteReply(Member member, Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        if (!reply.getWriter().getId().equals(member.getId())) {
            throw new AccessDeniedException("You can only delete your own reply.");
        }

        reply.delete();
        Feed feed = reply.getFeed();
        feed.removeComment(); // 댓글 수 감소
        feedRepository.save(feed);
    }

    @Transactional(readOnly = true)
    public Page<ReplyResponse> getReplies(Long feedId, Pageable pageable) {
        Page<Reply> page = replyRepository.findByFeedIdAndIsDeletedFalseOrderByCreatedAtDesc(feedId, pageable);
        return page.map(ReplyResponse::new); // 핵심
    }

}
