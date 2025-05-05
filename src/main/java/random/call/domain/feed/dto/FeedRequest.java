package random.call.domain.feed.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.feed.Feed;
import random.call.domain.member.Member;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedRequest {

    @NotBlank(message = "Content cannot be empty")
    private String content;

    private List<String> imageUrls;

    // FeedRequest -> Feed 변환

}