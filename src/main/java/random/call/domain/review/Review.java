package random.call.domain.review;


import jakarta.persistence.*;
import lombok.*;
import random.call.global.timeStamped.Timestamped;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Getter
@Builder
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reviewerId; // 리뷰어

    private Long targetId; // 받는사람

    private Long roomId;

    private Integer duration;

    private Integer score;

    private String title; // <- Enum name

    private String description; // 상세 내용

}
