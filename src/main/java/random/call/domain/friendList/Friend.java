package random.call.domain.friendList;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import random.call.domain.friendList.type.FriendStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberA;
    private Long memberB;

    @Enumerated(EnumType.STRING)
    private FriendStatus status = FriendStatus.ACTIVE; // 친구 상태 (활성, 차단, 삭제 등)

    private boolean isBlocked = false; // 차단 여부

    @CreationTimestamp
    private LocalDateTime createdAt; // 친구 관계 생성 시간


}
