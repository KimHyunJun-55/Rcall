package random.call.domain.review;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.review.dto.ReviewRequestDTO;
import random.call.domain.review.dto.ReviewResponseDTO;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    //내가받은 리뷰 전체조회
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){
        
        List<ReviewResponseDTO> list= reviewService.getMyReviews(jwtUserDetails);
                
        return ResponseEntity.ok(list);
    }

    //리뷰 등록
    @PostMapping
    public ResponseEntity<Boolean> createReview(@AuthenticationPrincipal JwtUserDetails jwtUserDetails, ReviewRequestDTO reviewRequestDTO){
        reviewService.createReview(jwtUserDetails,reviewRequestDTO);
        return ResponseEntity.ok(true);
    }



}
