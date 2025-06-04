package random.call.domain.feed.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.domain.Page;
import random.call.domain.feed.Feed;
import random.call.domain.feed.Location;
import random.call.domain.reply.Reply;
import random.call.domain.reply.dto.ReplyResponse;

import java.util.Collections;
import java.util.List;


@Getter
public class FeedResponse extends FeedBaseResponse {
    private final WriterDto writer;
    private final Integer commentCount;
    private final Integer likeCount;
    private final Location location;
    private final List<ReplyResponse> replies;
    private final Boolean isLiked;
    private final Boolean isReport;

    public FeedResponse(Feed feed, Boolean isLiked,Boolean isReport, Page<Reply> replies) {
        super(feed);
        this.writer = new WriterDto(
                feed.getWriter().getId(),
                feed.getWriter().getNickname(),
                feed.getWriter().getProfileImage()
        );
        this.likeCount = feed.getLikeCount() != null ? feed.getLikeCount() : 0;
        this.commentCount = feed.getCommentCount() != null ? feed.getCommentCount() : 0;
        this.location = feed.getLocation();
        this.replies = replies != null ?
                replies.stream().map(ReplyResponse::new).toList() :
                Collections.emptyList(); // null → 빈 리스트
        this.isLiked = isLiked != null ? isLiked : false;
        this.isReport = isReport != null ? isReport : false;
    }

    @Getter
    @AllArgsConstructor
    public static class WriterDto {
        private Long id;
        private String nickname;
        private String profileImage;
    }


}


