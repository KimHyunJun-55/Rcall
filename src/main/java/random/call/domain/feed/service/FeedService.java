package random.call.domain.feed.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;
import random.call.domain.feed.dto.FeedRequest;
import random.call.domain.feed.dto.FeedRequestByMemberIdDTO;
import random.call.domain.feed.dto.FeedResponse;
import random.call.domain.feed.dto.FeedSimpleResponseDTO;
import random.call.domain.friendList.FriendRepository;
import random.call.domain.like.LikeRepository;
import random.call.domain.member.Member;
import random.call.domain.reply.Reply;
import random.call.domain.reply.dto.ReplyRepository;
import random.call.domain.report.ReportRepository;

import java.util.*;

import static random.call.domain.report.type.ReportType.FEED;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final LikeRepository likeRepository;
    private final ReplyRepository replyRepository;
    private final ReportRepository reportRepository;
    private final FriendRepository friendRepository;

    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeeds(Long memberId, Pageable pageable) {
        // 1. 현재 사용자가 차단한 멤버 ID 목록 조회
        List<Long> blockedMemberIds = friendRepository.findBlockedMembersByMemberId(memberId);

        // 2. 차단된 사용자의 피드를 제외하고 조회
        Page<Feed> feedPage = feedRepository.findAllExcludingBlockedMembers(blockedMemberIds, pageable);

        return feedPage.map(feed -> buildFeedResponse(feed, memberId));
    }

    @Transactional(readOnly = true)
    public Page<FeedSimpleResponseDTO> getFeedsSimple(Long memberId, Pageable pageable) {
        List<Long> blockedMemberIds = friendRepository.findBlockedMembersByMemberId(memberId);

        Page<Feed> feedPage = feedRepository.findAllExcludingBlockedMembers(blockedMemberIds, pageable);

        return feedPage.map(
                feed -> {
                    boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
                    return FeedSimpleResponseDTO.from(feed,isLiked);
                }
        );
    };


    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedByFeedIdBefore(Long memberId, Long feedId, Pageable pageable) {
        // 1. 차단된 사용자 목록 조회
        List<Long> blockedMemberIds = friendRepository.findBlockedMembersByMemberId(memberId);

        // 2. 차단된 사용자 제외 + feedId보다 작은 피드 조회 (최신순)
        Page<Feed> feedPage = feedRepository.findByIdLessThanAndMemberNotInOrderByIdDesc(
                feedId,
                blockedMemberIds,
                pageable
        );

        return feedPage.map(feed -> buildFeedResponse(feed, memberId));
    }

    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedByFeedIdAfter(Long memberId, Long feedId, Pageable pageable) {
        // 1. 차단된 사용자 목록 조회
        List<Long> blockedMemberIds = friendRepository.findBlockedMembersByMemberId(memberId);

        // 2. 차단된 사용자 제외 + feedId보다 큰 피드 조회 (오래된순)
        Page<Feed> feedPage = feedRepository.findByIdGreaterThanAndMemberNotInOrderByIdAsc(
                feedId,
                blockedMemberIds,
                pageable
        );

        return feedPage.map(feed -> buildFeedResponse(feed, memberId));
    }

    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long memberId, Long feedId) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(()->new EntityNotFoundException("해당 피드를 찾을 수 없습니다."));

        return buildFeedResponse(feed, memberId);

    }
    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedsByMemberId(Long targetId,Long senderId, Pageable pageable) {
        Pageable sortedPageable = getPageable(pageable);

        Page<Feed> feedPage = feedRepository.findAllByMemberId(targetId,sortedPageable);
        return feedPage.map(feed -> buildFeedResponse(feed, senderId));

    }

    @Transactional(readOnly = true)
    public Page<FeedRequestByMemberIdDTO> getSimpleFeedsByMemberId(Long targetId,Long senderId, Pageable pageable) {
        Pageable sortedPageable = getPageable(pageable);

        Page<Feed> feedPage = feedRepository.findAllByMemberId(targetId,sortedPageable);
        return feedPage.map(FeedRequestByMemberIdDTO::new);
    }

    private FeedResponse buildFeedResponse(Feed feed, Long memberId) {
        boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
        boolean isReport = reportRepository.existsByReporterIdAndTargetIdAndReportType(memberId, feed.getId(), FEED);
        Page<Reply> replies = getReplyList(feed);

        return new FeedResponse(feed, isLiked, isReport);
    }


    //임시로
    @Transactional
    public void createFeed(Member member, FeedRequest request) {

        List<String> imageUrls = request.getImageUrls();
        // Feed 객체 생성
        Feed feed = Feed.builder()
                .writer(member)
                .title(request.getTitle())
                .content(request.getContent())
                .location(request.getLocation())
                .visibility(request.getVisibility())
                .category(request.getCategory())
                .imageUrls(imageUrls)
                .build();

        feedRepository.save(feed);
    }


    // Feed 수정
    @Transactional
    public void updateFeed(Long memberId, Long feedId, FeedRequest request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));
        // 2. 작성자 검증 (선택적)
        if (!feed.getWriter().getId().equals(memberId)) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        feed.update(request);


    }


    private static Pageable getPageable(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }


    private boolean needLoadReplies(Feed feed) {
        return feed.getCommentCount() != null && feed.getCommentCount() > 0;
    }
    private Page<Reply> getReplyList(Feed feed) {
        if (!needLoadReplies(feed)) {
            return Page.empty(); // ★ Collections.emptyList() 대신 Page.empty() 사용
        }
        return replyRepository.findByFeedIdAndIsDeletedFalseOrderByCreatedAtDesc(
                feed.getId(),
                PageRequest.of(0, 5)
        );
    }


}
