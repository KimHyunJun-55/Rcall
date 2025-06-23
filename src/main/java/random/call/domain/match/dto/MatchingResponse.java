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
    private MatchType matchType;  // 매칭 타입 (CHAT/CALL)
    private Long roomId;         // 생성된 방 ID (성공 시)
    private Object matchedUser;  // 매칭된 상대방 정보
    private String token;        // 통화용 토큰 (CALL 시)

    // 추가된 에러 처리 필드
    private boolean success;     // 성공 여부 (기본값 true)
    private String errorMessage; // 실패 시 오류 메시지

    // 성공 응답용 생성자 (기존과 호환 유지)
    public MatchingResponse(MatchType matchType, Long roomId, Object matchedUser, String token) {
        this.matchType = matchType;
        this.roomId = roomId;
        this.matchedUser = matchedUser;
        this.token = token;
        this.success = true;
        this.errorMessage = null;
    }

    // 에러 응답용 팩토리 메서드
    public static MatchingResponse error(MatchType matchType, String errorMessage) {
        return MatchingResponse.builder()
                .matchType(matchType)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}