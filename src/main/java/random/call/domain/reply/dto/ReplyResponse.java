package random.call.domain.reply.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import random.call.domain.feed.dto.FeedResponse;
import random.call.domain.reply.Reply;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReplyResponse {
    private Long id;
    private WriterDto writer;
    private String content;
    private LocalDateTime createdAt;

    public ReplyResponse(Reply reply) {
        this.id = reply.getId();
        this.writer = new ReplyResponse.WriterDto(reply.getWriter().getId(), reply.getWriter().getNickname(),reply.getWriter().getProfileImage());
        this.content = reply.getContent();
        this.createdAt = reply.getCreatedAt();
    }

    @Getter
    @AllArgsConstructor
    public static class WriterDto {
        private Long id;
        private String nickname;
        private String profileImage;
    }
}