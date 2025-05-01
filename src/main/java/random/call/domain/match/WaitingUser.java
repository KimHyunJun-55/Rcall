package random.call.domain.match;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WaitingUser {
    private Long userId;
    private long waitStartTime;

    // 생성자 추가
    public WaitingUser(Long userId) {
        this.userId = userId;
        this.waitStartTime = System.currentTimeMillis();
    }
}
