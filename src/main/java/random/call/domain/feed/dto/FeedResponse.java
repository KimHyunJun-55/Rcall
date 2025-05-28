package random.call.domain.feed.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import random.call.domain.feed.Feed;
import random.call.domain.feed.Location;


@Getter
public class FeedResponse extends FeedBaseResponse {

    private final WriterDto writer;
    private final Boolean isLiked;
    private final Integer commentCount;
    private final Integer likeCount;
    private final Location location;

    public FeedResponse(Feed feed, Boolean isLiked) {
        super(feed);
        this.writer = new WriterDto(
                feed.getWriter().getId(),
                feed.getWriter().getNickname(),
                feed.getWriter().getProfileImage()
        );
        this.isLiked = isLiked;
        this.likeCount = feed.getLikeCount();
        this.commentCount = feed.getCommentCount();
        this.location = feed.getLocation();
    }

    @Getter
    @AllArgsConstructor
    public static class WriterDto {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}


