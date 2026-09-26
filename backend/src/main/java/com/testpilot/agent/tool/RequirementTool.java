package com.testpilot.agent.tool;

import com.testpilot.requirement.Requirement;
import com.testpilot.requirement.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequirementTool implements AgentTool {

    private final RequirementService requirementService;

    @Override
    public String getName() {
        return "readRequirement";
    }

    @Override
    public String getDescription() {
        return "Read project requirements and extract business rules, test conditions, and functional specifications.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "requirementId": {"type": "integer"},
                "projectId": {"type": "integer"}
            },
            "required": ["projectId"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        Long projectId = args.get("projectId") != null ? ((Number) args.get("projectId")).longValue() : context.getProjectId();
        Long requirementId = args.get("requirementId") != null ? ((Number) args.get("requirementId")).longValue() : null;

        try {
            if (requirementId != null) {
                Requirement req = requirementService.getRequirement(requirementId);
                Map<String, Object> data = Map.of(
                        "id", req.getId(),
                        "title", req.getTitle(),
                        "description", req.getDescription() != null ? req.getDescription() : "",
                        "riskLevel", req.getRiskLevel(),
                        "coverageScore", req.getCoverageScore()
                );
                return ToolResult.ok("Read requirement: " + req.getTitle(), data);
            } else {
                List<Requirement> reqs = requirementService.getProjectRequirements(projectId);
                List<Map<String, Object>> dataList = reqs.stream().map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "title", r.getTitle(),
                        "description", r.getDescription() != null ? r.getDescription() : "",
                        "riskLevel", r.getRiskLevel(),
                        "coverageScore", r.getCoverageScore()
                )).toList();
                return ToolResult.ok("Found " + reqs.size() + " requirements", dataList);
            }
        } catch (Exception e) {
            return ToolResult.fail("Failed to read requirements: " + e.getMessage());
        }
    }
}
