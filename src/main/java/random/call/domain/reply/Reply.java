package random.call.domain.reply;

import jakarta.persistence.*;
import lombok.*;
import random.call.domain.feed.Feed;
import random.call.domain.member.Member;
import random.call.global.timeStamped.Timestamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reply extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @Column(nullable = false,length = 1200)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
