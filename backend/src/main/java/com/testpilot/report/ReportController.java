package com.testpilot.report;

import com.testpilot.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TestReportRepository reportRepository;

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<TestReport>> getProjectReports(@PathVariable Long projectId) {
        return ApiResponse.ok(reportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestReport> getReport(@PathVariable Long id) {
        return ApiResponse.ok(reportRepository.findById(id).orElseThrow());
    }
}
