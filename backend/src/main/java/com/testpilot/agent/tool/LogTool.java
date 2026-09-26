package com.testpilot.agent.tool;

import com.testpilot.agent.log.ApplicationLog;
import com.testpilot.agent.log.ApplicationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogTool implements AgentTool {

    private final ApplicationLogService logService;

    @Override
    public String getName() {
        return "readApplicationLog";
    }

    @Override
    public String getDescription() {
        return "Read application logs to analyze errors, exceptions, stack traces, and SQL errors for failure investigation.";
    }

    @Override
    public String getParametersSchema() {
        return """
        {
            "type": "object",
            "properties": {
                "level": {"type": "string", "enum": ["ERROR","WARN","INFO"]},
                "keyword": {"type": "string"},
                "sinceMinutes": {"type": "integer"},
                "maxLines": {"type": "integer"}
            },
            "required": ["level"]
        }""";
    }

    @Override
    public ToolResult execute(ToolContext context, Map<String, Object> args) {
        String level = (String) args.getOrDefault("level", "ERROR");
        String keyword = (String) args.get("keyword");
        int sinceMinutes = args.get("sinceMinutes") != null ? ((Number) args.get("sinceMinutes")).intValue() : 30;
        int maxLines = args.get("maxLines") != null ? ((Number) args.get("maxLines")).intValue() : 50;

        long start = System.currentTimeMillis();
        try {
            Long projectId = context.getProjectId();
            List<ApplicationLog> logs;

            if (keyword != null && !keyword.isEmpty()) {
                logs = logService.findByProjectAndLevelAndKeyword(projectId, level, keyword, sinceMinutes);
            } else {
                logs = logService.findByProjectAndLevel(projectId, level, sinceMinutes);
            }

            if (logs.size() > maxLines) {
                logs = logs.subList(0, maxLines);
            }

            List<Map<String, Object>> data = logs.stream().map(l -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", l.getId());
                map.put("level", l.getLevel());
                map.put("logger", l.getLogger());
                map.put("message", l.getMessage());
                map.put("exceptionClass", l.getExceptionClass());
                map.put("createdAt", l.getCreatedAt());
                return map;
            }).collect(Collectors.toList());

            long latency = System.currentTimeMillis() - start;
            return ToolResult.builder()
                    .success(true)
                    .output("Found " + logs.size() + " log entries at " + level + " level")
                    .data(data)
                    .latencyMs(latency)
                    .build();
        } catch (Exception e) {
            return ToolResult.fail("Failed to read logs: " + e.getMessage());
        }
    }
}
