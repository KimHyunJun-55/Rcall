package random.call.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import random.call.domain.member.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_participant_member_room", columnList = "member_id, chat_room_id")
})
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime joinedAt;

    @Setter
    private Long lastReadMessageId;

}