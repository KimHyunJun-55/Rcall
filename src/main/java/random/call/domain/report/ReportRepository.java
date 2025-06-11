package random.call.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import random.call.domain.report.type.ReportType;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetIdAndReportType(Long reporterId, Long targetId, ReportType reportType);

    List<Report> findByReporterId(Long id);
}

