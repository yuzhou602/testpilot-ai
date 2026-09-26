package com.testpilot.agent.runtime;

import com.testpilot.agent.tool.AgentTool;
import com.testpilot.agent.tool.AgentToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StepExecutor {

    private final AgentToolRegistry toolRegistry;

    public StepResult execute(String toolName, Map<String, Object> toolInput, AgentTool.ToolContext context) {
        long start = System.currentTimeMillis();

        try {
            if (toolName == null || toolName.isEmpty()) {
                return StepResult.builder()
                        .success(true)
                        .output("No tool required - pure reasoning step")
                        .latencyMs(System.currentTimeMillis() - start)
                        .build();
            }

            AgentTool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                return StepResult.builder()
                        .success(false)
                        .error("Tool not found: " + toolName)
                        .latencyMs(System.currentTimeMillis() - start)
                        .build();
            }

            AgentTool.ToolResult result = tool.execute(context, toolInput);
            long latency = System.currentTimeMillis() - start;

            return StepResult.builder()
                    .success(result.isSuccess())
                    .output(result.getOutput())
                    .data(result.getData())
                    .error(result.getError())
                    .latencyMs(latency)
                    .toolName(toolName)
                    .build();

        } catch (Exception e) {
            return StepResult.builder()
                    .success(false)
                    .error("Tool execution failed: " + e.getMessage())
                    .latencyMs(System.currentTimeMillis() - start)
                    .toolName(toolName)
                    .build();
        }
    }

    @Data
    @Builder
    public static class StepResult {
        private boolean success;
        private String output;
        private Object data;
        private String error;
        private Long latencyMs;
        private String toolName;
    }
}
