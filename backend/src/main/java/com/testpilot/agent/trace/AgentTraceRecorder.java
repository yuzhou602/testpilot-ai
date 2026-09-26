package com.testpilot.agent.trace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentTraceRecorder {

    private final AgentTraceRepository traceRepository;

    public AgentTrace record(Long taskId, int stepIndex, String eventType,
                              String input, String output, String toolName,
                              String status, Long latencyMs, Long tokensUsed,
                              String model, String error) {
        AgentTrace trace = AgentTrace.builder()
                .taskId(taskId)
                .stepIndex(stepIndex)
                .eventType(eventType)
                .input(truncate(input))
                .output(truncate(output))
                .toolName(toolName)
                .status(status)
                .latencyMs(latencyMs)
                .tokensUsed(tokensUsed)
                .model(model)
                .error(error)
                .build();

        AgentTrace saved = traceRepository.save(trace);
        log.debug("Trace recorded: taskId={}, step={}, event={}", taskId, stepIndex, eventType);
        return saved;
    }

    public List<AgentTrace> getTaskTraces(Long taskId) {
        return traceRepository.findByTaskIdOrderByStepIndexCreatedAt(taskId);
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 10000 ? s.substring(0, 10000) : s;
    }
}
