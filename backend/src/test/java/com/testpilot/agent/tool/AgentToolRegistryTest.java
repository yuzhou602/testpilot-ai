package com.testpilot.agent.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AgentToolRegistryTest {

    @InjectMocks
    private AgentToolRegistry registry;

    @BeforeEach
    void setUp() {
        // Register a test tool
        registry.register(new AgentTool() {
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
                return ToolResult.ok("Test result", null);
            }
        });
    }

    @Test
    void shouldRegisterTool() {
        AgentTool tool = registry.getTool("testTool");
        assertNotNull(tool);
        assertEquals("testTool", tool.getName());
    }

    @Test
    void shouldReturnNullForUnknownTool() {
        AgentTool tool = registry.getTool("unknownTool");
        assertNull(tool);
    }

    @Test
    void shouldGetAllTools() {
        var tools = registry.getAllTools();
        assertTrue(tools.size() >= 1);
    }
}
