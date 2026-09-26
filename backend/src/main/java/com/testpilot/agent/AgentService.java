package com.testpilot.agent;

import com.testpilot.agent.runtime.AgentExecutor;
import com.testpilot.agent.trace.AgentTrace;
import com.testpilot.agent.trace.AgentTraceRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentTaskRepository taskRepository;
    private final AgentStepRepository stepRepository;
    private final AgentExecutor executor;
    private final AgentTraceRecorder traceRecorder;

    public AgentTask createTask(AgentController.CreateTaskRequest request) {
        return executor.createTask(
                request.getProjectId(),
                request.getUserId(),
                request.getGoal(),
                request.getContext()
        );
    }

    public AgentTask getTask(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public List<AgentTask> getProjectTasks(Long projectId) {
        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public void executeTask(Long taskId) {
        executor.executeTask(taskId);
    }

    public void cancelTask(Long taskId) {
        executor.cancelTask(taskId);
    }

    public SseEmitter createSseEmitter(Long taskId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes
        executor.registerEmitter(taskId, emitter);
        return emitter;
    }

    public List<AgentStep> getTaskSteps(Long taskId) {
        return stepRepository.findByTaskIdOrderByStepIndex(taskId);
    }

    public List<AgentTrace> getTaskTraces(Long taskId) {
        return traceRecorder.getTaskTraces(taskId);
    }
}
