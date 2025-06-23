package random.call.domain.like;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;
import random.call.domain.like.dto.LikeToggleResponse;
import random.call.domain.member.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final FeedRepository feedRepository;


    @Transactional
    public LikeToggleResponse likeToggle(Long memberId, Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        Likes like = likeRepository.findByMemberIdAndFeedId(memberId, feedId)
                .orElseGet(() -> Likes.builder()
                        .memberId(memberId)
                        .feedId(feedId)
                        .isLike(false)
                        .build());

        boolean before = like.getIsLike();
        like.toggle(); // 토글 수행

        if (before && !like.getIsLike()) {
            feed.decreaseLikeCount();
        } else if (!before && like.getIsLike()) {
            feed.increaseLikeCount();
        }

        likeRepository.save(like);
        feedRepository.save(feed);

        return LikeToggleResponse.builder()
                .likeCount(feed.getLikeCount())
                .isLiked(like.getIsLike())
                .build();
    }

    public void deleteLikes(Member member){
        List<Likes> likes = likeRepository.findByMemberId(member.getId());
        likeRepository.deleteAll(likes);
        log.info("{} 회원의 좋아요 삭제 프로세스 완료",member.getId());
    }
}
