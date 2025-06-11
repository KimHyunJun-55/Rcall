package random.call.domain.review;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.review.dto.ReviewRequestDTO;
import random.call.domain.review.dto.ReviewResponseDTO;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;


    //내리뷰 전체조회
    @Transactional
    public List<ReviewResponseDTO> getMyReviews(JwtUserDetails jwtUserDetails) {
        List<Review> reviews = reviewRepository.findByTargetId(jwtUserDetails.id());

        return reviews.stream()
                .map(review -> {
                    Member writer = getMember(review.getReviewerId());
                    return new ReviewResponseDTO(review, writer);
                })
                .collect(Collectors.toList());
    }

    //리뷰 작성
    @Transactional
    public void createReview(JwtUserDetails jwtUserDetails,ReviewRequestDTO request){
        String title = ReviewType.fromId(request.titleId()).getLabel();

        Review review = Review
                .builder()
                .reviewerId(jwtUserDetails.id())
                .roomId(request.roomId())
                .title(title)
                .description(request.description())
                .targetId(request.targetId())
                .score(request.score())
                .duration(request.duration())
                .build();

        reviewRepository.save(review);
    }
    


    private Member getMember(Long memberId){
        return memberRepository.findById(memberId).orElseThrow(()-> new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));

    }
}
