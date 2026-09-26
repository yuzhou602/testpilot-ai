package com.testpilot.agent.evaluation;

import com.testpilot.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final AgentEvaluationRepository evaluationRepository;

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<AgentEvaluation>> getProjectEvaluations(@PathVariable Long projectId) {
        return ApiResponse.ok(evaluationRepository.findByProjectIdOrderByEvaluatedAtDesc(projectId));
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<List<AgentEvaluation>> getTaskEvaluations(@PathVariable Long taskId) {
        return ApiResponse.ok(evaluationRepository.findByTaskId(taskId));
    }
}
