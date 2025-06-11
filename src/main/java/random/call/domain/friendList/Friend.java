package random.call.domain.friendList;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import random.call.domain.friendList.type.FriendStatus;
import random.call.global.timeStamped.Timestamped;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Friend extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberA;
    private Long memberB;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FriendStatus status = FriendStatus.ACTIVE; // 친구 상태 (활성, 차단, 삭제 등)

    private LocalDateTime blockedAt; // 친구 관계 생성 시간

    @Column(nullable = true)
    private Long blockedBy;

    public void blockUpdate(){
        this.status=FriendStatus.BLOCKED;
        this.blockedAt = LocalDateTime.now();
    }

}
