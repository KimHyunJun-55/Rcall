package random.call.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import random.call.domain.report.Report;
import random.call.domain.report.ReportRepository;
import random.call.domain.report.dto.ReportRequest;
import random.call.domain.report.type.ReportType;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public void createReport(Long reporterId, ReportRequest request) {
        ReportType type = ReportType.fromId(request.getReportTypeId());

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetId(request.getTargetId())
                .reportType(type)
                .details(request.getDetails())
                .build();

        reportRepository.save(report);
    }
}

