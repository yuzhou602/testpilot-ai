package com.testpilot.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class AgentToolRegistry {

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();

    public AgentToolRegistry(List<AgentTool> toolList) {
        for (AgentTool tool : toolList) {
            tools.put(tool.getName(), tool);
            log.info("Registered tool: {} - {}", tool.getName(), tool.getDescription());
        }
    }

    public AgentTool getTool(String name) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return tool;
    }

    public List<AgentTool> getAllTools() {
        return new ArrayList<>(tools.values());
    }

    public List<Map<String, String>> getToolDescriptions() {
        List<Map<String, String>> descriptions = new ArrayList<>();
        for (AgentTool tool : tools.values()) {
            Map<String, String> desc = new HashMap<>();
            desc.put("name", tool.getName());
            desc.put("description", tool.getDescription());
            desc.put("parameters", tool.getParametersSchema());
            descriptions.add(desc);
        }
        return descriptions;
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
}
