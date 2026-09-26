package com.testpilot.agent.multi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentContext {

    private Long projectId;
    private Long taskId;
    private String conversationId;
    private Map<String, Object> metadata;

    public static AgentContext create(Long projectId, Long taskId) {
        return AgentContext.builder()
                .projectId(projectId)
                .taskId(taskId)
                .build();
    }
}
