package random.call.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchingStatusResponse {
    private String status;  // PROGRESS | MATCHED | FAILED | ERROR
    private String message;
    private long timestamp;
}
