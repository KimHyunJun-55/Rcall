package random.call.domain.feed.dto;

import lombok.Data;
import random.call.domain.feed.Feed;

@Data
public class FeedRequestByMemberIdDTO {


    private Long id;
    private String title;
    private String content;

    public FeedRequestByMemberIdDTO(Feed feed){
        this.id = feed.getId();
        this.title = feed.getTitle();
        this.content = feed.getContent();
    }
}
