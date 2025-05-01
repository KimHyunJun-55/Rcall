package random.call.domain.reply.dto;

import lombok.Getter;
import random.call.domain.reply.Reply;

import java.time.LocalDateTime;

@Getter
public class ReplyResponse {
    private Long id;
    private String writerNickname;
    private String content;
    private LocalDateTime createdAt;

    public ReplyResponse(Reply reply) {
        this.id = reply.getId();
        this.writerNickname = reply.getWriter().getNickname(); // 닉네임 있다고 가정
        this.content = reply.getContent();
        this.createdAt = reply.getCreatedAt();
    }
}