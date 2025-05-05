package random.call.domain.feed.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import random.call.domain.feed.Feed;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class FeedResponse {

    private Long id;
    private WriterDto writer;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;

    public FeedResponse(Feed feed) {
        this.id = feed.getId();
        this.writer = new WriterDto(feed.getWriter().getId(), feed.getWriter().getNickname());
        this.content = feed.getContent();
        this.imageUrls = feed.getImageUrls();
        this.likeCount = feed.getLikeCount();
        this.commentCount = feed.getCommentCount();
        this.createdAt = feed.getCreatedAt();
    }

    @Getter
    @AllArgsConstructor
    public static class WriterDto {
        private Long id;
        private String nickname;
    }
}


