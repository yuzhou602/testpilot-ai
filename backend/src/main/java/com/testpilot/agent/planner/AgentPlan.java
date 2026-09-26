package com.testpilot.agent.planner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentPlan {
    private String goal;
    private String strategy;
    private List<Map<String, Object>> scenarios;
    private List<Map<String, Object>> steps;
    private String estimatedDuration;
    private List<String> riskAreas;
}
