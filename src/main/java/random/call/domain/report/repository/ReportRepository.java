package random.call.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import random.call.domain.report.Report;
import random.call.domain.report.type.ReportType;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetIdAndReportType(Long reporterId, Long targetId, ReportType reportType);

    List<Report> findByReporterId(Long id);
}

