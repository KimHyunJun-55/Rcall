package random.call.domain.report;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.report.dto.ReportRequest;
import random.call.domain.report.dto.ReportResponseDTO;
import random.call.domain.report.service.ReportService;
import random.call.global.security.userDetails.JwtUserDetails;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<?> getReports(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){
        List<ReportResponseDTO> lists = reportService.getReports(jwtUserDetails.id());
        return ResponseEntity.ok(lists);
    }
}