package random.call.domain.like.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;

@Data
@Builder
public class LikeToggleResponse {
    private Boolean isLiked;
    private Integer likeCount;
}
