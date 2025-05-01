package random.call.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoiceMatchResponse {
    private String channelName;
    private String token;
    private String status;
}