package random.call.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.report.Report;
import random.call.domain.report.ReportRepository;
import random.call.domain.report.dto.ReportRequest;
import random.call.domain.report.dto.ReportResponseDTO;
import random.call.domain.report.type.*;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public void createReport(Long reporterId, ReportRequest request) {
        String titleLabel = resolveTitleLabel(request.type(), request.titleId());

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetId(request.targetId())
                .reportType(request.type())
                .title(titleLabel)
                .details(request.description())
                .build();

        reportRepository.save(report);
    }

    private String resolveTitleLabel(ReportType type, int titleId) {
        return switch (type) {
            case FEED -> PostReportTitleType.fromId(titleId).getLabel();
            case CHAT -> ChatReportTitleType.fromId(titleId).getLabel();
            case CALL -> CallReportTitleType.fromId(titleId).getLabel();
            case REPLY -> ReplyReportTitleType.fromId(titleId).getLabel();
        };
    }

    public List<ReportResponseDTO> getReports(Long id) {
        List<Report> reports = reportRepository.findByReporterId(id);
        return reports.stream().map(ReportResponseDTO::new).toList();
    }
}


