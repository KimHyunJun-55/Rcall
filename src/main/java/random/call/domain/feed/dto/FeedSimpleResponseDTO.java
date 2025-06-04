package random.call.domain.feed.dto;

import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import random.call.domain.feed.Feed;
import random.call.domain.feed.type.Category;
import random.call.domain.feed.type.Visibility;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class FeedSimpleResponseDTO {
    private Long id;
    private String title;
    private List<String> imageUrls;
    private Category category;
    private LocalDateTime createdAt;
    private Visibility visibility;
    private FeedResponse.WriterDto writer;
    private Integer commentCount;
    private Integer likeCount;

    private  Boolean isLiked;


    public static FeedSimpleResponseDTO from(Feed feed, Boolean isLiked){
        return FeedSimpleResponseDTO.builder()
                .id(feed.getId())
                .title(feed.getTitle())
                .imageUrls(feed.getImageUrls())
                .category(feed.getCategory())
                .createdAt(feed.getCreatedAt())
                .visibility(feed.getVisibility())
                .writer( new FeedResponse.WriterDto(
                        feed.getWriter().getId(),
                        feed.getWriter().getNickname(),
                        feed.getWriter().getProfileImage()

                ))
                .commentCount(feed.getCommentCount())
                .likeCount(feed.getLikeCount())
                .isLiked(isLiked)
                .build();
    }


}
