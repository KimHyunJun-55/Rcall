package random.call.domain.feed.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import random.call.domain.feed.Feed;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FeedResponse {

    private Long id;             // Feed ID
    private String content;      // 게시물 내용
    private String writerName;   // 작성자 이름 또는 닉네임
    private String writerProfilePic; // 작성자 프로필 사진 URL (선택 사항)
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 업데이트 시간

    // Entity -> DTO 변환
    public FeedResponse(Feed feed) {
        this.id = feed.getId();
        this.content = feed.getContent();
        this.writerName = feed.getWriter().getNickname(); // 예시로 writer의 이름을 가져옵니다.
        this.writerProfilePic = feed.getWriter().getProfileImage(); // 예시로 프로필 사진을 가져옵니다.
        this.createdAt = feed.getCreatedAt();
        this.updatedAt = feed.getModifiedAt();
    }
}
