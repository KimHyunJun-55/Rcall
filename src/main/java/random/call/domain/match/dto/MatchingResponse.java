package random.call.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import random.call.domain.match.MatchType;

@Data
@AllArgsConstructor
@Builder
public class MatchingResponse {


    private MatchType matchType;
    private Long roomId;
    private Object matchedUser;
    private String token; // 전화용 필드 (nullable)
//    private String roomName; // 전화용 필드 (nullable)
}
