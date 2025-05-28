package random.call.domain.feed.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.feed.Feed;
import random.call.domain.feed.FeedRepository;
import random.call.domain.feed.dto.FeedBaseResponse;
import random.call.domain.feed.dto.FeedRequest;
import random.call.domain.feed.dto.FeedRequestByMemberIdDTO;
import random.call.domain.feed.dto.FeedResponse;
import random.call.domain.like.LikeRepository;
import random.call.domain.member.Member;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeeds(Long memberId, Pageable pageable) {
        Pageable sortedPageable = getPageable(pageable);

        Page<Feed> feedPage = feedRepository.findAll(sortedPageable);

        return feedPage.map(feed -> {
            boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
            return new FeedResponse(feed, isLiked);
        });
    }

    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedByFeedIdBefore(Long memberId, Long feedId, Pageable pageable) {
        // 최신순(내림차순)으로 정렬된 Feed 중 기준 ID보다 작은 것들
        Page<Feed> feedPage = feedRepository.findByIdLessThanOrderByIdDesc(feedId, pageable);
        System.out.println(pageable.getPageSize());

        return feedPage.map(feed -> {
            boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
            return new FeedResponse(feed, isLiked);
        });
    }

    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedByFeedIdAfter(Long memberId, Long feedId, Pageable pageable) {
        // 오래된순(오름차순)으로 정렬된 Feed 중 기준 ID보다 큰 것들
        System.out.println(pageable.getPageSize());
        Page<Feed> feedPage = feedRepository.findByIdGreaterThanOrderByIdAsc(feedId, pageable);

        return feedPage.map(feed -> {
            boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
            return new FeedResponse(feed, isLiked);
        });
    }

    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long memberId, Long feedId) {

        Feed feed = feedRepository.findById(feedId).orElseThrow(()->new EntityNotFoundException("해당 피드를 찾을 수 없습니다."));

            boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(memberId, feed.getId());
            return new FeedResponse(feed, isLiked);

    }
    @Transactional(readOnly = true)
    public Page<FeedResponse> getFeedsByMemberId(Long targetId,Long senderId, Pageable pageable) {
        Pageable sortedPageable = getPageable(pageable);

        Page<Feed> feedPage = feedRepository.findAllByMemberId(targetId,sortedPageable);
        return feedPage.map(feed -> {
            boolean isLiked = likeRepository.existsByMemberIdAndFeedIdAndIsLikeTrue(targetId, feed.getId());
            return new FeedResponse(feed, isLiked);
        });
    }

    @Transactional(readOnly = true)
    public Page<FeedRequestByMemberIdDTO> getSimpleFeedsByMemberId(Long targetId,Long senderId, Pageable pageable) {
        Pageable sortedPageable = getPageable(pageable);

        Page<Feed> feedPage = feedRepository.findAllByMemberId(targetId,sortedPageable);
        return feedPage.map(FeedRequestByMemberIdDTO::new);
    }

    // Feed 조회 (단일 Feed)
//    @Transactional(readOnly = true)
//    public FeedResponse getFeed(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, Long feedId) {
//        Feed feed = feedRepository.findById(feedId)
//                .orElseThrow(() -> new EntityNotFoundException("Feed not found"));
//        return new FeedResponse(jwtUserDetails.id(),feed);
//    }

    // Feed 작성
//    @Transactional
//    public void createFeed(Member member, FeedRequest request) {
//
//        Feed feed = Feed.builder()
//                .writer(member)
//                .content(request.getContent())
//                .imageUrls(request.getImageUrls()) // 이미지 URL 목록도 추가
//                .build(); // 빌더 패턴을 통해 Feed 객체 생성
//
//        feedRepository.save(feed); // 저장
//    }



    //임시로
    @Transactional
    public void createFeed(Member member, FeedRequest request) {

        // 기본 이미지 목록
        List<String> dummyImages = Arrays.asList(
                "https://www.fitpetmall.com/wp-content/uploads/2023/10/shutterstock_1275055966-1.png",
                "https://cdn.news.hidoc.co.kr/news/photo/202205/27398_65438_0638.jpg",
                "https://images.pexels.com/photos/45201/kitty-cat-kitten-pet-45201.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/416160/pexels-photo-416160.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/774731/pexels-photo-774731.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/982865/pexels-photo-982865.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/2558605/pexels-photo-2558605.jpeg?auto=compress&cs=tinysrgb&w=600",

                //강아지
                "https://images.pexels.com/photos/31936184/pexels-photo-31936184.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/31921795/pexels-photo-31921795.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/1448055/pexels-photo-1448055.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/2664417/pexels-photo-2664417.jpeg?auto=compress&cs=tinysrgb&w=600",
                "https://images.pexels.com/photos/26791702/pexels-photo-26791702.jpeg?auto=compress&cs=tinysrgb&w=600"
        );

        // request에서 이미지 URL이 비어있다면 랜덤으로 선택
        List<String> imageUrls = request.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            Random random = new Random();
            int numImages = random.nextInt(3) + 1; // 1개에서 3개 사이의 숫자
            imageUrls = new ArrayList<>();

            for (int i = 0; i < numImages; i++) {
                imageUrls.add(dummyImages.get(random.nextInt(dummyImages.size())));
            }
        }

        // Feed 객체 생성
        Feed feed = Feed.builder()
                .writer(member)
                .title(request.getTitle())
                .content(request.getContent())
                .location(request.getLocation())
                .visibility(request.getVisibility())
                .category(request.getCategory())
                .imageUrls(imageUrls) // 랜덤으로 선택된 이미지 URL 목록 추가
                .build();

        feedRepository.save(feed); // 저장
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


}
