package random.call.domain.feed;


import jakarta.persistence.*;
import lombok.*;
import random.call.domain.feed.dto.FeedRequest;
import random.call.domain.feed.type.Category;
import random.call.domain.feed.type.Visibility;
import random.call.domain.member.Member;
import random.call.global.timeStamped.Timestamped;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자는 외부에서 호출할 수 없도록 제한
@Table(indexes = @Index(name = "idx_feed_created_at", columnList = "createdAt"))

@Builder
public class Feed extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer; // 작성자
    @Column(nullable = false, length = 20)
    private String title;
    @Column(nullable = false, length = 300)
    private String content; // 게시물 내용

    @ElementCollection
    @CollectionTable(name = "feed_image_urls", joinColumns = @JoinColumn(name = "feed_id"))
    @Column(name = "image_urls", length = 400)
    private List<String> imageUrls; // 이미지 URL 목록

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0; // 좋아요 수

    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0; // 댓글 수

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC; // 게시물 가시성 (PUBLIC, PRIVATE 등)

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "lng"))
    })
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Category category = Category.ETC;

    @Column
    @Builder.Default
    private Boolean isDeleted = false; // 삭제 여부

    // 좋아요 추가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 제거
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    // 댓글 수 증가
    public void addComment() {
        this.commentCount++;
    }

    // 댓글 수 감소
    public void removeComment() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    // Feed 객체 수정용 빌더 메서드 추가 (기존 Feed 객체를 기반으로 수정)
    public FeedBuilder toBuilder() {
        return builder()
                .id(this.id)
                .writer(this.writer)
                .content(this.content)
                .imageUrls(this.imageUrls)
                .likeCount(this.likeCount)
                .commentCount(this.commentCount)
                .visibility(this.visibility)
                .isDeleted(this.isDeleted);
    }

    // feed 객체를 생성하는 빌더 메서드 (기본 생성자는 외부에서 호출할 수 없으므로 빌더를 사용)
    public void update(FeedRequest feedRequest) {
        if (title != null) {
            this.title = feedRequest.getTitle();
        }
        if (content != null) {
            this.content = feedRequest.getContent();
        }
        if (imageUrls != null) {
            this.imageUrls = feedRequest.getImageUrls();
        }
        if (visibility != null) {
            this.visibility = feedRequest.getVisibility();
        }
        if(category != null){
            this.category =feedRequest.getCategory();
        }
    }
}
