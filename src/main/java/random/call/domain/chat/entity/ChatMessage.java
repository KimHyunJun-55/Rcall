package random.call.domain.chat.entity;


import jakarta.persistence.*;
import lombok.*;
import random.call.domain.member.Member;
import random.call.global.timeStamped.Timestamped;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
