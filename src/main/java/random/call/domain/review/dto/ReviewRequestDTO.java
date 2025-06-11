package random.call.domain.review.dto;


import random.call.domain.report.type.ReportType;

public record ReviewRequestDTO(
        Long targetId,
        Long roomId,
        Integer titleId,
        String description,
        Integer score,
        Integer duration
) {
}
