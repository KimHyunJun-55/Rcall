package random.call.domain.match;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "match_table")  // 테이블 이름을 변경
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requesterId;

    private Long matchedUserId;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    private String channelName;

    @Column(columnDefinition = "TEXT")
    private String token;

    public enum MatchStatus {
        WAITING, MATCHED, CANCELLED, FAILED
    }
}
