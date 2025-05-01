package random.call.domain.feed.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;
import random.call.domain.feed.dto.FeedRequest;
import random.call.domain.feed.dto.FeedResponse;
import random.call.domain.member.Member;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    // Feed 조회 (페이징 처리)
    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeeds(Pageable pageable) {
        return feedRepository.findAll(pageable)
                .map(FeedResponse::new);
    }

    // Feed 조회 (단일 Feed)
    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));
        return new FeedResponse(feed);
    }

    // Feed 작성
    @Transactional
    public void createFeed(Member member, FeedRequest request) {

        Feed feed = Feed.builder()
                .writer(member)
                .content(request.getContent())
                .imageUrls(request.getImageUrls()) // 이미지 URL 목록도 추가
                .build(); // 빌더 패턴을 통해 Feed 객체 생성

        feedRepository.save(feed); // 저장
    }

    // Feed 수정
    @Transactional
    public void updateFeed(Member member, Long feedId, FeedRequest request) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));

        // content만 수정 (빌더를 사용하여 새 객체를 생성)
        Feed updatedFeed = feed.toBuilder()  // toBuilder()를 통해 기존 객체 기반으로 수정
                .content(request.getContent())
                .build();

        feedRepository.save(updatedFeed); // 저장
    }
}
