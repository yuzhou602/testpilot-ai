package com.testpilot.agent.tool;

import com.testpilot.common.enums.AgentTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public interface AgentTool {

    String getName();

    String getDescription();

    String getParametersSchema();

    ToolResult execute(ToolContext context, Map<String, Object> args);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ToolContext {
        private Long taskId;
        private Long projectId;
        private AgentTaskStatus taskStatus;
        private Map<String, Object> metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ToolResult {
        private boolean success;
        private String output;
        private Object data;
        private String error;
        private Long latencyMs;

        public static ToolResult ok(String output, Object data) {
            return ToolResult.builder().success(true).output(output).data(data).build();
        }

        public static ToolResult fail(String error) {
            return ToolResult.builder().success(false).error(error).build();
        }
    }
}
