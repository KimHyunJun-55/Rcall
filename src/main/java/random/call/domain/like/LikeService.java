package random.call.domain.like;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final FeedRepository feedRepository;


    @Transactional
    public boolean likeToggle(Long memberId, Long feedId) {

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        Like like = likeRepository.findByMemberIdAndFeedId(memberId, feedId)
                .orElseGet(() -> Like.builder()
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

        return like.getIsLike();
    }
}
