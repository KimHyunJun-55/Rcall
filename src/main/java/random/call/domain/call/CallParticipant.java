package random.call.domain.call;


import jakarta.persistence.*;
import lombok.*;
import random.call.domain.member.Member;
import random.call.global.timeStamped.Timestamped;

import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_participant_member_room", columnList = "member_id, call_room_id")
})
public class CallParticipant extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "call_room_id")
    private CallRoom callRoom;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

}
