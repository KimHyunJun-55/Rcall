package random.call.domain.report.dto;

import lombok.Getter;
import random.call.domain.report.Report;
import random.call.domain.report.type.ReportStatus;
import random.call.domain.report.type.ReportType;

import java.time.LocalDateTime;

@Getter
public class ReportResponseDTO {

    private final Long id;
    private final String title;
    private final String detail;
    private final String result;
    private final ReportType type;
    private final ReportStatus status;
    private final LocalDateTime createdAt;



    public ReportResponseDTO(Report report){
        this.id =report.getId();
        this.title=report.getTitle();
        this.detail=report.getDetails();
        this.result=report.getResult();
        this.type=report.getReportType();
        this.status=report.getStatus();
        this.createdAt=report.getCreatedAt();


    }
}
