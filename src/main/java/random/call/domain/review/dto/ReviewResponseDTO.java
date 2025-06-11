package random.call.domain.review.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import random.call.domain.member.Member;
import random.call.domain.review.Review;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDTO {

    private final Long id;
    private final Integer score;
    private final String description;
    private final Integer duration;
    private final LocalDateTime createAt;
    private final WriterDto writer;

    public ReviewResponseDTO(Review review,Member member){
        this.id =review.getId();
        this.score =review.getScore();
        this.description = review.getDescription();
        this.duration = review.getDuration();
        this.createAt = review.getCreatedAt();
        this.writer = new WriterDto(member.getId(),member.getNickname(), member.getProfileImage());
    }

    @Getter
    @AllArgsConstructor
    public static class WriterDto {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}
