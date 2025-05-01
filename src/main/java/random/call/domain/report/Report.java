package random.call.domain.report;

import jakarta.persistence.*;
import lombok.*;
import random.call.domain.report.type.ReportType;
import random.call.global.timeStamped.Timestamped;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId; // 신고자

    private Long targetId;   // 신고 대상 (ex. Feed ID 또는 Member ID 등)

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    private String details; // 상세 내용
}
