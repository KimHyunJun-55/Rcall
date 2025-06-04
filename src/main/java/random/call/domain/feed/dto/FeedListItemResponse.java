package random.call.domain.feed.dto;

import java.util.List;

public record FeedListItemResponse(
        Long id,
        String title,
        WriterDto writer,
        Integer likeCount,
        Integer commentCount,
        List<String> imageUrls
) {
    public record WriterDto(Long id, String nickname, String profileImage) {}
}
