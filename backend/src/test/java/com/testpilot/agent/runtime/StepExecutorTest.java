package com.testpilot.agent.runtime;

import com.testpilot.agent.tool.AgentTool;
import com.testpilot.agent.tool.AgentToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepExecutorTest {

    @InjectMocks
    private StepExecutor stepExecutor;

    @Mock
    private AgentToolRegistry toolRegistry;

    private AgentTool.ToolContext testContext;

    @BeforeEach
    void setUp() {
        testContext = AgentTool.ToolContext.builder()
                .taskId(1L)
                .projectId(1L)
                .build();
    }

    @Test
    void shouldExecuteToolSuccessfully() {
        AgentTool mockTool = new AgentTool() {
            @Override
            public String getName() {
                return "testTool";
            }

            @Override
            public String getDescription() {
                return "Test tool";
            }

            @Override
            public String getParametersSchema() {
                return "{}";
            }

            @Override
            public ToolResult execute(ToolContext context, Map<String, Object> args) {
                return ToolResult.ok("Success", Map.of("key", "value"));
            }
        };

        when(toolRegistry.getTool("testTool")).thenReturn(mockTool);

        StepExecutor.StepResult result = stepExecutor.execute("testTool", Map.of(), testContext);

        assertTrue(result.isSuccess());
        assertEquals("Success", result.getOutput());
    }

    @Test
    void shouldReturnErrorForNullTool() {
        StepExecutor.StepResult result = stepExecutor.execute(null, Map.of(), testContext);

        assertTrue(result.isSuccess());
        assertEquals("No tool required - pure reasoning step", result.getOutput());
    }

    @Test
    void shouldReturnErrorForUnknownTool() {
        when(toolRegistry.getTool("unknownTool")).thenReturn(null);

        StepExecutor.StepResult result = stepExecutor.execute("unknownTool", Map.of(), testContext);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Tool not found"));
    }

    @Test
    void shouldHandleToolExecutionException() {
        AgentTool mockTool = new AgentTool() {
            @Override
            public String getName() {
                return "failingTool";
            }

            @Override
            public String getDescription() {
                return "Failing tool";
            }

            @Override
            public String getParametersSchema() {
                return "{}";
            }

            @Override
            public ToolResult execute(ToolContext context, Map<String, Object> args) {
                throw new RuntimeException("Tool execution failed");
            }
        };

        when(toolRegistry.getTool("failingTool")).thenReturn(mockTool);

        StepExecutor.StepResult result = stepExecutor.execute("failingTool", Map.of(), testContext);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Tool execution failed"));
    }
}
