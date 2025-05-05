package random.call.domain.friendRequest;

import jakarta.persistence.*;
import lombok.*;
import random.call.domain.friendRequest.type.FriendRequestStatus;
import random.call.global.timeStamped.Timestamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FriendRequest extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;     // 요청한 사람
    private Long receiverId;   // 요청받은 사람

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus  status;

    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
    }
}
