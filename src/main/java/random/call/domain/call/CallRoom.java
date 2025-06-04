package random.call.domain.call;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import random.call.global.timeStamped.Timestamped;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class CallRoom extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;




}
