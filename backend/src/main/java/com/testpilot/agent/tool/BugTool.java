package com.testpilot.agent.tool;

import com.testpilot.bug.BugService;
import com.testpilot.bug.TestBug;
import com.testpilot.common.enums.BugSeverity;
import com.testpilot.common.enums.BugStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BugTool implements AgentTool {

    private final BugService bugService;

    @Override
    public String getName() {
        return "createBug";
    }

    @Override
    public String getDescription() {
        return "Create a structured bug report with root cause hypothesis, evidence, and suggested fix. Requires human confirmation before finalizing.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "title": {"type": "string"},
                "description": {"type": "string"},
                "severity": {"type": "string", "enum": ["BLOCKER","CRITICAL","MAJOR","MINOR","TRIVIAL"]},
                "module": {"type": "string"},
                "stepsToReproduce": {"type": "string"},
                "expectedResult": {"type": "string"},
                "actualResult": {"type": "string"},
                "rootCauseHypothesis": {"type": "string"},
                "confidence": {"type": "number"},
                "suspectedModule": {"type": "string"},
                "evidence": {"type": "string"},
                "suggestedFix": {"type": "string"}
            },
            "required": ["title", "actualResult"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        try {
            String severity = (String) args.getOrDefault("severity", "MAJOR");

            TestBug bug = TestBug.builder()
                    .projectId(context.getProjectId())
                    .executionId(context.getTaskId())
                    .title((String) args.get("title"))
                    .description((String) args.get("description"))
                    .severity(BugSeverity.valueOf(severity))
                    .status(BugStatus.OPEN)
                    .module((String) args.get("module"))
                    .stepsToReproduce((String) args.get("stepsToReproduce"))
                    .expectedResult((String) args.get("expectedResult"))
                    .actualResult((String) args.get("actualResult"))
                    .rootCauseHypothesis((String) args.get("rootCauseHypothesis"))
                    .confidence(args.get("confidence") != null ? ((Number) args.get("confidence")).doubleValue() : null)
                    .suspectedModule((String) args.get("suspectedModule"))
                    .evidence((String) args.get("evidence"))
                    .suggestedFix((String) args.get("suggestedFix"))
                    .aiGenerated(true)
                    .confirmed(false)
                    .build();

            TestBug saved = bugService.createBug(bug);

            return ToolResult.builder()
                    .success(true)
                    .output("Bug created: " + saved.getTitle() + " [" + saved.getSeverity() + "]")
                    .data(Map.of(
                            "bugId", saved.getId(),
                            "title", saved.getTitle(),
                            "severity", saved.getSeverity(),
                            "status", saved.getStatus(),
                            "confidence", saved.getConfidence() != null ? saved.getConfidence() : "N/A",
                            "confirmed", saved.getConfirmed()
                    ))
                    .build();
        } catch (Exception e) {
            return ToolResult.fail("Failed to create bug: " + e.getMessage());
        }
    }
}
