package random.call.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Builder
public class MatchingResponse {

    private Long roomId;
    private String agoraToken;
    private String channelName;
    private String matchedAt;
    private Long matchMemberId;
    private String matchMember;
}
