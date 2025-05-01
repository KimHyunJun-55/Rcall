package random.call.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private Long targetId;
    private int reportTypeId;
    private String details;
}
