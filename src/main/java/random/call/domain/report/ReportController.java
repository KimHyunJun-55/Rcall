package random.call.domain.report;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.report.dto.ReportRequest;
import random.call.domain.report.service.ReportService;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {


    private final ReportService reportService;

    @PostMapping("")
    public ResponseEntity<Void> createReport(@AuthenticationPrincipal JwtUserDetails userDetails,
                                             @RequestBody ReportRequest request) {
        reportService.createReport(userDetails.id(), request);
        return ResponseEntity.ok().build();
    }
}
