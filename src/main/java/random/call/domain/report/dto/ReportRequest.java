package random.call.domain.report.dto;


import random.call.domain.report.type.ReportType;

public record ReportRequest(
        ReportType type,
        Long targetId,
        Integer titleId,
        String description,
        String targetNickname
) {}
