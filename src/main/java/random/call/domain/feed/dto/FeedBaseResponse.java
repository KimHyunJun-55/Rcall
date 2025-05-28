package random.call.domain.feed.dto;

import lombok.Getter;
import random.call.domain.feed.Feed;
import random.call.domain.feed.Location;
import random.call.domain.feed.type.Category;
import random.call.domain.feed.type.Visibility;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class FeedBaseResponse {
    protected Long id;
    protected String title;
    protected String content;
    protected List<String> imageUrls;
    protected LocalDateTime createdAt;
    protected Category category;
    protected Visibility visibility;



    public FeedBaseResponse(Feed feed) {
        this.id = feed.getId();
        this.title = feed.getTitle();
        this.content = feed.getContent();
        this.imageUrls = feed.getImageUrls();
        this.createdAt = feed.getCreatedAt();
        this.category = feed.getCategory();
        this.visibility = feed.getVisibility();

    }
}